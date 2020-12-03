package tgw.evolution.entities.projectiles;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.util.Gravity;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.NBTTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public abstract class EntityGenericProjectile extends Entity implements IProjectile, IEntityAdditionalSpawnData {
    private static final DataParameter<Byte> PIERCE_LEVEL = EntityDataManager.createKey(EntityGenericProjectile.class, DataSerializers.BYTE);
    public byte arrowShake;
    public boolean inGround;
    public PickupStatus pickupStatus = PickupStatus.ALLOWED;
    @Nullable
    public UUID shootingEntity;
    public int ticksInAir;
    public int timeInGround;
    private float damage = 2.0f;
    private List<Entity> hitEntities;
    private SoundEvent hitSound = this.getHitEntitySound();
    @Nullable
    private BlockState inBlockState;
    private double mass = 1;
    private IntOpenHashSet piercedEntities;
    private int ticksInGround;

    public EntityGenericProjectile(EntityType<? extends EntityGenericProjectile> type, LivingEntity shooter, World worldIn, double mass) {
        this(type, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.1F, shooter.posZ, worldIn);
        this.setShooter(shooter);
        this.mass = mass;
    }

    public EntityGenericProjectile(EntityType<? extends EntityGenericProjectile> type, double x, double y, double z, World worldIn) {
        this(type, worldIn);
        this.setPosition(x, y, z);
    }

    public EntityGenericProjectile(EntityType<? extends EntityGenericProjectile> type, World worldIn) {
        super(type, worldIn);
        this.preventEntitySpawning = true;
    }

    @Override
    public boolean canBeAttackedWithItem() {
        return false;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    protected abstract ItemStack getArrowStack();

    public float getDamage() {
        return this.damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected float getEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return 0.0F;
    }

    protected SoundEvent getHitEntitySound() {
        return SoundEvents.ENTITY_ARROW_HIT;
    }

    public byte getPierceLevel() {
        return this.dataManager.get(PIERCE_LEVEL);
    }

    public void setPierceLevel(byte level) {
        this.dataManager.set(PIERCE_LEVEL, level);
    }

    @Nullable
    public LivingEntity getShooter() {
        return this.shootingEntity != null && this.world instanceof ServerWorld ?
               (LivingEntity) ((ServerWorld) this.world).getEntityByUuid(this.shootingEntity) :
               null;
    }

    public void setShooter(@Nullable LivingEntity entity) {
        this.shootingEntity = entity == null ? null : entity.getUniqueID();
        if (entity instanceof PlayerEntity) {
            this.pickupStatus = ((PlayerEntity) entity).abilities.isCreativeMode ?
                                EntityGenericProjectile.PickupStatus.CREATIVE_ONLY :
                                EntityGenericProjectile.PickupStatus.ALLOWED;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = this.getBoundingBox().getAverageEdgeLength() * 10;
        if (Double.isNaN(d0)) {
            d0 = 1;
        }
        d0 = d0 * 64 * getRenderDistanceWeight();
        return distance < d0 * d0;
    }

    @Override
    public void onCollideWithPlayer(PlayerEntity entityIn) {
        if (!this.world.isRemote && this.inGround && this.arrowShake <= 0) {
            boolean canBePickedUp = this.pickupStatus == PickupStatus.ALLOWED ||
                                    this.pickupStatus == PickupStatus.CREATIVE_ONLY && entityIn.abilities.isCreativeMode;
            if (this.pickupStatus == PickupStatus.ALLOWED && !entityIn.inventory.addItemStackToInventory(this.getArrowStack())) {
                canBePickedUp = false;
            }
            if (canBePickedUp) {
                entityIn.onItemPickup(this, 1);
                this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.2f, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7f + 1) * 2);
                this.remove();
            }
        }
    }

    protected void onEntityHit(EntityRayTraceResult entityRayTrace) {
        Entity rayTracedEntity = entityRayTrace.getEntity();
        float velocityLength = (float) this.getMotion().length();
        float damage = velocityLength * this.damage;
        if (this.getPierceLevel() > 0) {
            if (this.piercedEntities == null) {
                this.piercedEntities = new IntOpenHashSet(5);
            }
            if (this.hitEntities == null) {
                this.hitEntities = Lists.newArrayListWithCapacity(5);
            }
            if (this.piercedEntities.size() >= this.getPierceLevel() + 1) {
                this.remove();
                return;
            }
            this.piercedEntities.add(rayTracedEntity.getEntityId());
        }
        LivingEntity shooter = this.getShooter();
        DamageSource source;
        if (shooter == null) {
            source = EvolutionDamage.causeArrowDamage(this, this);
        }
        else {
            source = EvolutionDamage.causeArrowDamage(this, shooter);
            shooter.setLastAttackedEntity(rayTracedEntity);
        }
        int j = rayTracedEntity.getFireTimer();
        if (this.isBurning() && !(rayTracedEntity instanceof EndermanEntity)) {
            rayTracedEntity.setFire(5);
        }
        if (rayTracedEntity.attackEntityFrom(source, damage)) {
            if (rayTracedEntity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity) rayTracedEntity;
                if (!this.world.isRemote && this.getPierceLevel() <= 0) {
                    livingentity.setArrowCountInEntity(livingentity.getArrowCountInEntity() + 1);
                }
                if (!this.world.isRemote && shooter != null) {
                    EnchantmentHelper.applyThornEnchantments(livingentity, shooter);
                    EnchantmentHelper.applyArthropodEnchantments(shooter, livingentity);
                }
                if (livingentity != shooter && livingentity instanceof PlayerEntity && shooter instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) shooter).connection.sendPacket(new SChangeGameStatePacket(6, 0.0F));
                }
                if (!rayTracedEntity.isAlive() && this.hitEntities != null) {
                    this.hitEntities.add(livingentity);
                }
            }
            this.playSound(this.hitSound, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0 && !(rayTracedEntity instanceof EndermanEntity)) {
                this.remove();
            }
        }
        else {
            rayTracedEntity.setFireTimer(j);
            this.setMotion(this.getMotion().scale(-0.1));
            this.rotationYaw += 180.0F;
            this.prevRotationYaw += 180.0F;
            this.ticksInAir = 0;
            if (!this.world.isRemote && this.getMotion().lengthSquared() < 1.0E-7) {
                if (this.pickupStatus == EntityGenericProjectile.PickupStatus.ALLOWED) {
                    this.entityDropItem(this.getArrowStack(), 0.1F);
                }
                this.remove();
            }
        }
    }

    protected void onHit(RayTraceResult rayTrace) {
        RayTraceResult.Type rayTraceType = rayTrace.getType();
        if (rayTraceType == RayTraceResult.Type.ENTITY) {
            this.onEntityHit((EntityRayTraceResult) rayTrace);
        }
        else if (rayTraceType == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult blockRayTrace = (BlockRayTraceResult) rayTrace;
            BlockState stateAtPos = this.world.getBlockState(blockRayTrace.getPos());
            this.inBlockState = stateAtPos;
            Vec3d vec3d = blockRayTrace.getHitVec().subtract(this.posX, this.posY, this.posZ);
            this.setMotion(vec3d);
            Vec3d vec3d1 = vec3d.normalize().scale(0.05);
            this.posX -= vec3d1.x;
            this.posY -= vec3d1.y;
            this.posZ -= vec3d1.z;
            this.playSound(this.hitSound, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            this.inGround = true;
            this.arrowShake = 7;
            this.setPierceLevel((byte) 0);
            this.hitSound = SoundEvents.ENTITY_ARROW_HIT;
            this.resetHitEntities();
            stateAtPos.onProjectileCollision(this.world, stateAtPos, blockRayTrace, this);
        }
    }

    @Nullable
    protected EntityRayTraceResult rayTraceEntities(Vec3d startVec, Vec3d endVec) {
        return ProjectileHelper.rayTraceEntities(this.world,
                                                 this,
                                                 startVec,
                                                 endVec,
                                                 this.getBoundingBox().expand(this.getMotion()).grow(1),
                                                 entity -> !entity.isSpectator() &&
                                                           entity.isAlive() &&
                                                           entity.canBeAttackedWithItem() &&
                                                           entity.canBeCollidedWith() &&
                                                           (entity != this.getShooter() || this.ticksInAir >= 5) &&
                                                           (this.piercedEntities == null || !this.piercedEntities.contains(entity.getEntityId())));
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        this.ticksInGround = compound.getShort("life");
        if (compound.contains("inBlockState", NBTTypes.COMPOUND_NBT.getId())) {
            this.inBlockState = NBTUtil.readBlockState(compound.getCompound("inBlockState"));
        }
        this.arrowShake = compound.getByte("shake");
        this.inGround = compound.getBoolean("inGround");
        this.damage = compound.getFloat("damage");
        this.mass = compound.getDouble("mass");
        this.pickupStatus = PickupStatus.getByOrdinal(compound.getByte("pickup"));
        this.setPierceLevel(compound.getByte("PierceLevel"));
        if (compound.hasUniqueId("OwnerUUID")) {
            this.shootingEntity = compound.getUniqueId("OwnerUUID");
        }
        if (compound.contains("SoundEvent", NBTTypes.STRING.getId())) {
            this.hitSound = Registry.SOUND_EVENT.getValue(new ResourceLocation(compound.getString("SoundEvent"))).orElseGet(this::getHitEntitySound);
        }
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        int id = buffer.readInt();
        if (id != 0) {
            this.setShooter((LivingEntity) this.world.getEntityByID(id));
        }
        this.mass = buffer.readDouble();
    }

    @Override
    protected void registerData() {
        this.dataManager.register(PIERCE_LEVEL, (byte) 0);
    }

    private void resetHitEntities() {
        if (this.hitEntities != null) {
            this.hitEntities.clear();
        }
        if (this.piercedEntities != null) {
            this.piercedEntities.clear();
        }
    }

    public void setHitSound(SoundEvent soundIn) {
        this.hitSound = soundIn;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setVelocity(double x, double y, double z) {
        this.setMotion(x, y, z);
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float horizontalLength = MathHelper.sqrt(x * x + z * z);
            this.rotationPitch = MathHelper.radToDeg((float) MathHelper.atan2(y, horizontalLength));
            this.rotationYaw = MathHelper.radToDeg((float) MathHelper.atan2(x, z));
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            this.ticksInGround = 0;
        }
    }

    public void shoot(Entity shooter, float pitch, float yaw, float velocity, float inaccuracy) {
        float x = -MathHelper.sinDeg(yaw) * MathHelper.cosDeg(pitch);
        float y = -MathHelper.sinDeg(pitch);
        float z = MathHelper.cosDeg(yaw) * MathHelper.cosDeg(pitch);
        this.shoot(x, y, z, velocity, inaccuracy);
        this.setMotion(this.getMotion().add(shooter.getMotion().x, shooter.onGround ? 0 : shooter.getMotion().y, shooter.getMotion().z));
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3d motion = new Vec3d(x, y, z).normalize()
                                         .add(this.rand.nextGaussian() * 0.007_5F * inaccuracy,
                                              this.rand.nextGaussian() * 0.007_5F * inaccuracy,
                                              this.rand.nextGaussian() * 0.007_5F * inaccuracy)
                                         .scale(velocity);
        this.setMotion(motion);
        float horizontalLength = MathHelper.sqrt(horizontalMag(motion));
        this.rotationYaw = MathHelper.radToDeg((float) MathHelper.atan2(motion.x, motion.z));
        this.rotationPitch = MathHelper.radToDeg((float) MathHelper.atan2(motion.y, horizontalLength));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
        this.ticksInGround = 0;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3d motion = this.getMotion();
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float horizontalSpeed = MathHelper.horizontalLength(motion);
            this.rotationYaw = MathHelper.radToDeg((float) MathHelper.atan2(motion.x, motion.z));
            this.rotationPitch = MathHelper.radToDeg((float) MathHelper.atan2(motion.y, horizontalSpeed));
            this.prevRotationYaw = this.rotationYaw;
            this.prevRotationPitch = this.rotationPitch;
        }
        BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
        BlockState stateAtPos = this.world.getBlockState(pos);
        if (!stateAtPos.isAir(this.world, pos)) {
            VoxelShape shape = stateAtPos.getCollisionShape(this.world, pos);
            if (!shape.isEmpty()) {
                Vec3d positionVec = new Vec3d(this.posX, this.posY, this.posZ);
                for (AxisAlignedBB boundingBox : shape.toBoundingBoxList()) {
                    if (boundingBox.offset(pos).contains(positionVec)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }
        if (this.arrowShake > 0) {
            --this.arrowShake;
        }
        if (this.isWet()) {
            this.extinguish();
        }
        if (this.inGround) {
            ++this.timeInGround;
            if (this.inBlockState != stateAtPos && this.world.areCollisionShapesEmpty(this.getBoundingBox().grow(0.06))) {
                this.inGround = false;
//                this.setMotion(motion.mul(this.rand.nextFloat() * 0.2F, this.rand.nextFloat() * 0.2F, this.rand.nextFloat() * 0.2F));
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            }
            else if (!this.world.isRemote) {
                this.tryDespawn();
            }
        }
        else {
            this.timeInGround = 0;
            ++this.ticksInAir;
            Vec3d positionVec = new Vec3d(this.posX, this.posY, this.posZ);
            Vec3d newPositionVec = positionVec.add(motion);
            RayTraceResult rayTrace = this.world.rayTraceBlocks(new RayTraceContext(positionVec,
                                                                                    newPositionVec,
                                                                                    RayTraceContext.BlockMode.COLLIDER,
                                                                                    RayTraceContext.FluidMode.NONE,
                                                                                    this));
            if (rayTrace.getType() != RayTraceResult.Type.MISS) {
                newPositionVec = rayTrace.getHitVec();
            }
            while (!this.removed) {
                EntityRayTraceResult entityRayTrace = this.rayTraceEntities(positionVec, newPositionVec);
                if (entityRayTrace != null) {
                    rayTrace = entityRayTrace;
                }
                if (rayTrace != null && rayTrace.getType() == RayTraceResult.Type.ENTITY) {
                    Entity rayTracedEntity = ((EntityRayTraceResult) rayTrace).getEntity();
                    Entity shooter = this.getShooter();
                    if (rayTracedEntity instanceof PlayerEntity &&
                        shooter instanceof PlayerEntity &&
                        !((PlayerEntity) shooter).canAttackPlayer((PlayerEntity) rayTracedEntity)) {
                        rayTrace = null;
                        entityRayTrace = null;
                    }
                }
                if (rayTrace != null && rayTrace.getType() != RayTraceResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, rayTrace)) {
                    this.onHit(rayTrace);
                    this.isAirBorne = true;
                }
                if (entityRayTrace == null || this.getPierceLevel() <= 0) {
                    break;
                }
                rayTrace = null;
            }
            motion = this.getMotion();
            double motionX = motion.x;
            double motionY = motion.y;
            double motionZ = motion.z;
            this.posX += motionX;
            this.posY += motionY;
            this.posZ += motionZ;
            float horizontalSpeed = MathHelper.horizontalLength(motion);
            this.rotationYaw = MathHelper.radToDeg((float) MathHelper.atan2(motionX, motionZ));
            //noinspection StatementWithEmptyBody
            for (this.rotationPitch = MathHelper.radToDeg((float) MathHelper.atan2(motionY, horizontalSpeed)); this.rotationPitch -
                                                                                                               this.prevRotationPitch <
                                                                                                               -180.0f; this.prevRotationPitch -= 360.0F) {
            }
            while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
                this.prevRotationPitch += 360.0F;
            }
            while (this.rotationYaw - this.prevRotationYaw < -180.0f) {
                this.prevRotationYaw -= 360.0F;
            }
            while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
                this.prevRotationYaw += 360.0F;
            }
            this.rotationPitch = MathHelper.lerp(0.2F, this.prevRotationPitch, this.rotationPitch);
            this.rotationYaw = MathHelper.lerp(0.2F, this.prevRotationYaw, this.rotationYaw);
            double horizontalDrag = this.isInWater() ? Gravity.horizontalWaterDrag(this) / this.mass : Gravity.horizontalDrag(this) / this.mass;
            double verticalDrag = this.isInWater() ? Gravity.verticalWaterDrag(this) / this.mass : Gravity.verticalDrag(this) / this.mass;
            double dragX = Math.signum(motionX) * motionX * motionX * horizontalDrag;
            if (Math.abs(dragX) > Math.abs(motionX)) {
                dragX = motionX;
            }
            double dragY = Math.signum(motionY) * motionY * motionY * verticalDrag;
            if (Math.abs(dragY) > Math.abs(motionY)) {
                dragY = motionY;
            }
            double dragZ = Math.signum(motionZ) * motionZ * motionZ * horizontalDrag;
            if (Math.abs(dragZ) > Math.abs(motionZ)) {
                dragZ = motionZ;
            }
            if (this.isInWater()) {
                for (int j = 0; j < 4; ++j) {
                    this.world.addParticle(ParticleTypes.BUBBLE,
                                           this.posX - motionX * 0.25,
                                           this.posY - motionY * 0.25,
                                           this.posZ - motionZ * 0.25,
                                           motionX,
                                           motionY,
                                           motionZ);
                }
            }
            double gravity = 0;
            if (!this.hasNoGravity()) {
                gravity = Gravity.gravity(this.world.dimension);
            }
            motionX -= dragX;
            motionY += -gravity - dragY;
            motionZ -= dragZ;
            this.setPosition(this.posX, this.posY, this.posZ);
            this.setMotion(motionX, motionY, motionZ);
            this.doBlockCollisions();
        }
    }

    protected void tryDespawn() {
        ++this.ticksInGround;
        if (this.ticksInGround >= 1_200) {
            this.remove();
        }
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        compound.putShort("life", (short) this.ticksInGround);
        if (this.inBlockState != null) {
            compound.put("inBlockState", NBTUtil.writeBlockState(this.inBlockState));
        }
        compound.putByte("shake", this.arrowShake);
        compound.putBoolean("inGround", this.inGround);
        compound.putByte("pickup", (byte) this.pickupStatus.ordinal());
        compound.putDouble("damage", this.damage);
        compound.putDouble("mass", this.mass);
        compound.putByte("PierceLevel", this.getPierceLevel());
        if (this.shootingEntity != null) {
            compound.putUniqueId("OwnerUUID", this.shootingEntity);
        }
        compound.putString("SoundEvent", Registry.SOUND_EVENT.getKey(this.hitSound).toString());
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        Entity shooter = this.getShooter();
        buffer.writeInt(shooter == null ? 0 : shooter.getEntityId());
        buffer.writeDouble(this.mass);
    }

    public enum PickupStatus {
        DISALLOWED,
        ALLOWED,
        CREATIVE_ONLY;

        public static PickupStatus getByOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal > values().length) {
                ordinal = 0;
            }
            return values()[ordinal];
        }
    }
}
