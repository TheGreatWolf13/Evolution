package tgw.evolution.entities.projectiles;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
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
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.entities.IEvolutionEntity;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.util.Gravity;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.NBTTypes;
import tgw.evolution.util.PlayerHelper;
import tgw.evolution.util.damage.DamageSourceEv;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class EntityGenericProjectile<T extends EntityGenericProjectile<T>> extends Entity implements IEntityAdditionalSpawnData,
                                                                                                              IEvolutionEntity<T> {
    private static final DataParameter<Byte> PIERCE_LEVEL = EntityDataManager.defineId(EntityGenericProjectile.class, DataSerializers.BYTE);
    protected final IntSet hitEntities = new IntOpenHashSet();
    public byte arrowShake;
    public boolean inGround;
    public PickupStatus pickupStatus = PickupStatus.ALLOWED;
    @Nullable
    public UUID shootingEntity;
    public int ticksInAir;
    public int timeInGround;
    private float damage = 2.0f;
    @Nullable
    private BlockState inBlockState;
    private double mass = 1;
    private IntOpenHashSet piercedEntities;
    private int ticksInGround;

    public EntityGenericProjectile(EntityType<? extends EntityGenericProjectile> type, LivingEntity shooter, World world, double mass) {
        this(type, shooter.getX(), shooter.getY() + shooter.getEyeHeight() - 0.1F, shooter.getZ(), world);
        this.setShooter(shooter);
        this.mass = mass;
    }

    public EntityGenericProjectile(EntityType<? extends EntityGenericProjectile> type, double x, double y, double z, World world) {
        this(type, world);
        this.setPos(x, y, z);
    }

    public EntityGenericProjectile(EntityType<? extends EntityGenericProjectile> type, World world) {
        super(type, world);
        this.blocksBuilding = true;
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        compound.putShort("TicksInGround", (short) this.ticksInGround);
        if (this.inBlockState != null) {
            compound.put("BlockStateIn", NBTUtil.writeBlockState(this.inBlockState));
        }
        compound.putByte("Shake", this.arrowShake);
        compound.putBoolean("InGround", this.inGround);
        compound.putByte("Pickup", (byte) this.pickupStatus.ordinal());
        compound.putDouble("Damage", this.damage);
        compound.putDouble("Mass", this.mass);
        compound.putByte("PierceLevel", this.getPierceLevel());
        if (this.shootingEntity != null) {
            compound.putUUID("OwnerUUID", this.shootingEntity);
        }
    }

    public void applyDamageActual(ServerPlayerEntity shooter, float damage, EvolutionDamage.Type type, LivingEntity entity) {
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_ACTUAL.get(type), damage);
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_ACTUAL.get(EvolutionDamage.Type.RANGED), damage);
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_ACTUAL.get(EvolutionDamage.Type.TOTAL), damage);
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT.get(), entity.getType(), damage);
    }

    public void applyDamageRaw(ServerPlayerEntity shooter, float damage, EvolutionDamage.Type type) {
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_RAW.get(type), damage);
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_RAW.get(EvolutionDamage.Type.RANGED), damage);
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_RAW.get(EvolutionDamage.Type.TOTAL), damage);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(PIERCE_LEVEL, (byte) 0);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    protected abstract ItemStack getArrowStack();

    public float getDamage() {
        return this.damage;
    }

    @Override
    protected float getEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return 0.0F;
    }

    protected SoundEvent getHitBlockSound() {
        return SoundEvents.ARROW_HIT;
    }

    public byte getPierceLevel() {
        return this.entityData.get(PIERCE_LEVEL);
    }

    @Nullable
    public LivingEntity getShooter() {
        return this.shootingEntity != null && this.level instanceof ServerWorld ?
               (LivingEntity) ((ServerWorld) this.level).getEntity(this.shootingEntity) :
               null;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void lerpMotion(double x, double y, double z) {
        this.setDeltaMovement(x, y, z);
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float horizontalLength = MathHelper.sqrt(x * x + z * z);
            this.xRot = MathHelper.radToDeg((float) MathHelper.atan2(y, horizontalLength));
            this.yRot = MathHelper.radToDeg((float) MathHelper.atan2(x, z));
            this.xRotO = this.xRot;
            this.yRotO = this.yRot;
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
            this.ticksInGround = 0;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.setPos(x, y, z);
        this.setRot(yaw, pitch);
    }

    protected void onEntityHit(EntityRayTraceResult entityRayTrace) {
        Entity rayTracedEntity = entityRayTrace.getEntity();
        float velocityLength = (float) this.getDeltaMovement().length();
        float damage = velocityLength * this.damage;
        if (this.getPierceLevel() > 0) {
            if (this.piercedEntities == null) {
                this.piercedEntities = new IntOpenHashSet(5);
            }
            if (this.piercedEntities.size() >= this.getPierceLevel() + 1) {
                this.remove();
                return;
            }
            this.piercedEntities.add(rayTracedEntity.getId());
        }
        LivingEntity shooter = this.getShooter();
        DamageSourceEv source;
        if (shooter == null) {
            source = EvolutionDamage.causeArrowDamage(this, this);
        }
        else {
            source = EvolutionDamage.causeArrowDamage(this, shooter);
            shooter.setLastHurtMob(rayTracedEntity);
        }
        int j = rayTracedEntity.getRemainingFireTicks();
        if (this.isOnFire() && !(rayTracedEntity instanceof EndermanEntity)) {
            rayTracedEntity.setRemainingFireTicks(5);
        }
        float oldHealth = rayTracedEntity instanceof LivingEntity ? ((LivingEntity) rayTracedEntity).getHealth() : 0;
        if (rayTracedEntity.hurt(source, damage)) {
            if (rayTracedEntity instanceof LivingEntity) {
                LivingEntity livingHit = (LivingEntity) rayTracedEntity;
                if (shooter instanceof ServerPlayerEntity) {
                    this.applyDamageRaw((ServerPlayerEntity) shooter, damage, source.getType());
                    float actualDamage = oldHealth - livingHit.getHealth();
                    this.applyDamageActual((ServerPlayerEntity) shooter, actualDamage, source.getType(), livingHit);
                }
                if (!this.level.isClientSide && this.getPierceLevel() <= 0) {
                    livingHit.setArrowCount(livingHit.getArrowCount() + 1);
                }
                if (!this.level.isClientSide && shooter != null) {
                    EnchantmentHelper.doPostHurtEffects(livingHit, shooter);
                    EnchantmentHelper.doPostDamageEffects(shooter, livingHit);
                }
                if (livingHit != shooter && livingHit instanceof PlayerEntity && shooter instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) shooter).connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.ARROW_HIT_PLAYER, 0.0F));
                }
                if (!rayTracedEntity.isAlive() && this.hitEntities != null) {
                    this.hitEntities.add(livingHit.getId());
                }
            }
            this.playSound(this.getHitBlockSound(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0 && !(rayTracedEntity instanceof EndermanEntity)) {
                this.remove();
            }
        }
        else {
            rayTracedEntity.setRemainingFireTicks(j);
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
            this.yRot += 180.0F;
            this.yRotO += 180.0F;
            this.ticksInAir = 0;
            if (!this.level.isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                if (this.pickupStatus == EntityGenericProjectile.PickupStatus.ALLOWED) {
                    this.spawnAtLocation(this.getArrowStack(), 0.1F);
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
            BlockState stateAtPos = this.level.getBlockState(blockRayTrace.getBlockPos());
            this.inBlockState = stateAtPos;
            Vector3d vec3d = blockRayTrace.getLocation().subtract(this.position());
            this.setDeltaMovement(vec3d);
            Vector3d vec3d1 = vec3d.normalize().scale(0.05);
            this.setPosRaw(this.getX() - vec3d1.x, this.getY() - vec3d1.y, this.getZ() - vec3d1.z);
            this.playSound(this.getHitBlockSound(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            this.inGround = true;
            this.arrowShake = 7;
            this.setPierceLevel((byte) 0);
            this.resetHitEntities();
            this.onProjectileCollision(this.level, stateAtPos, blockRayTrace);
        }
    }

    public void onProjectileCollision(World world, BlockState state, BlockRayTraceResult hit) {
        //TODO
//        state.getBlock().onProjectileCollision(world, state, hit, this);
    }

    @Override
    public void playerTouch(PlayerEntity player) {
        if (!this.level.isClientSide && this.inGround && this.arrowShake <= 0) {
            boolean canBePickedUp = this.pickupStatus == PickupStatus.ALLOWED ||
                                    this.pickupStatus == PickupStatus.CREATIVE_ONLY && player.abilities.instabuild;
            if (this.pickupStatus == PickupStatus.ALLOWED && !player.inventory.add(this.getArrowStack())) {
                canBePickedUp = false;
            }
            if (canBePickedUp) {
                player.take(this, 1);
                this.playSound(SoundEvents.ITEM_PICKUP, 0.2f, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7f + 1) * 2);
                this.remove();
            }
        }
    }

    @Nullable
    protected EntityRayTraceResult rayTraceEntities(Vector3d startVec, Vector3d endVec) {
        return ProjectileHelper.getEntityHitResult(this.level,
                                                   this,
                                                   startVec,
                                                   endVec,
                                                   this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1),
                                                   entity -> !entity.isSpectator() &&
                                                             entity.isAlive() &&
                                                             entity.isAttackable() &&
                                                             entity.isPickable() &&
                                                             (entity != this.getShooter() || this.ticksInAir >= 5) &&
                                                             (this.piercedEntities == null || !this.piercedEntities.contains(entity.getId())));
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        this.ticksInGround = compound.getShort("TicksInGround");
        if (compound.contains("BlockStateIn", NBTTypes.COMPOUND_NBT)) {
            this.inBlockState = NBTUtil.readBlockState(compound.getCompound("BlockStateIn"));
        }
        this.arrowShake = compound.getByte("Shake");
        this.inGround = compound.getBoolean("InGround");
        this.damage = compound.getFloat("Damage");
        this.mass = compound.getDouble("Mass");
        this.pickupStatus = PickupStatus.getByOrdinal(compound.getByte("Pickup"));
        this.setPierceLevel(compound.getByte("PierceLevel"));
        if (compound.hasUUID("OwnerUUID")) {
            this.shootingEntity = compound.getUUID("OwnerUUID");
        }
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        int id = buffer.readInt();
        if (id != 0) {
            this.setShooter((LivingEntity) this.level.getEntity(id));
        }
        this.mass = buffer.readDouble();
    }

    private void resetHitEntities() {
        if (this.hitEntities != null) {
            this.hitEntities.clear();
        }
        if (this.piercedEntities != null) {
            this.piercedEntities.clear();
        }
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setPierceLevel(byte level) {
        this.entityData.set(PIERCE_LEVEL, level);
    }

    public void setShooter(@Nullable LivingEntity entity) {
        this.shootingEntity = entity == null ? null : entity.getUUID();
        if (entity instanceof PlayerEntity) {
            this.pickupStatus = ((PlayerEntity) entity).abilities.instabuild ?
                                EntityGenericProjectile.PickupStatus.CREATIVE_ONLY :
                                EntityGenericProjectile.PickupStatus.ALLOWED;
        }
    }

    public void shoot(Entity shooter, float pitch, float yaw, float velocity, float inaccuracy) {
        float x = -MathHelper.sinDeg(yaw) * MathHelper.cosDeg(pitch);
        float y = -MathHelper.sinDeg(pitch);
        float z = MathHelper.cosDeg(yaw) * MathHelper.cosDeg(pitch);
        this.shoot(x, y, z, velocity, inaccuracy);
        this.setDeltaMovement(this.getDeltaMovement().add(shooter.getDeltaMovement().x, shooter.getDeltaMovement().y, shooter.getDeltaMovement().z));
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vector3d motion = new Vector3d(x, y, z).normalize()
                                               .add(this.random.nextGaussian() * 0.007_5F * inaccuracy,
                                                    this.random.nextGaussian() * 0.007_5F * inaccuracy,
                                                    this.random.nextGaussian() * 0.007_5F * inaccuracy)
                                               .normalize()
                                               .scale(velocity);
        this.setDeltaMovement(motion);
        float horizontalLength = MathHelper.sqrt(getHorizontalDistanceSqr(motion));
        this.yRot = MathHelper.radToDeg((float) MathHelper.atan2(motion.x, motion.z));
        this.xRot = MathHelper.radToDeg((float) MathHelper.atan2(motion.y, horizontalLength));
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
        this.ticksInGround = 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 10;
        if (Double.isNaN(d0)) {
            d0 = 1;
        }
        d0 = d0 * 64 * getViewScale();
        return distance < d0 * d0;
    }

    @Override
    public void tick() {
        super.tick();
        Vector3d motion = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float horizontalSpeed = MathHelper.horizontalLength(motion);
            this.yRot = MathHelper.radToDeg((float) MathHelper.atan2(motion.x, motion.z));
            this.xRot = MathHelper.radToDeg((float) MathHelper.atan2(motion.y, horizontalSpeed));
            this.yRotO = this.yRot;
            this.xRotO = this.xRot;
        }
        BlockPos pos = this.blockPosition();
        BlockState stateAtPos = this.level.getBlockState(pos);
        if (!stateAtPos.isAir(this.level, pos)) {
            VoxelShape shape = stateAtPos.getCollisionShape(this.level, pos);
            if (!shape.isEmpty()) {
                Vector3d positionVec = this.position();
                for (AxisAlignedBB boundingBox : shape.toAabbs()) {
                    if (boundingBox.move(pos).contains(positionVec)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }
        if (this.arrowShake > 0) {
            --this.arrowShake;
        }
        if (this.isInWaterOrRain()) {
            this.clearFire();
        }
        if (this.inGround) {
            ++this.timeInGround;
            if (this.inBlockState != stateAtPos && this.level.noCollision(this.getBoundingBox().inflate(0.06))) {
                this.inGround = false;
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            }
            else if (!this.level.isClientSide) {
                this.tryDespawn();
            }
        }
        else {
            this.timeInGround = 0;
            this.ticksInAir++;
            Vector3d positionVec = this.position();
            Vector3d newPositionVec = positionVec.add(motion);
            RayTraceResult rayTrace = this.level.clip(new RayTraceContext(positionVec,
                                                                          newPositionVec,
                                                                          RayTraceContext.BlockMode.COLLIDER,
                                                                          RayTraceContext.FluidMode.NONE,
                                                                          this));
            if (rayTrace.getType() != RayTraceResult.Type.MISS) {
                newPositionVec = rayTrace.getLocation();
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
                        !((PlayerEntity) shooter).canAttack((PlayerEntity) rayTracedEntity)) {
                        rayTrace = null;
                        entityRayTrace = null;
                    }
                }
                if (rayTrace != null && rayTrace.getType() != RayTraceResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, rayTrace)) {
                    this.onHit(rayTrace);
                    this.hasImpulse = true;
                }
                if (entityRayTrace == null || this.getPierceLevel() <= 0) {
                    break;
                }
                rayTrace = null;
            }
            motion = this.getDeltaMovement();
            double motionX = motion.x;
            double motionY = motion.y;
            double motionZ = motion.z;
            this.setPosRaw(this.getX() + motionX, this.getY() + motionY, this.getZ() + motionZ);
            float horizontalSpeed = MathHelper.horizontalLength(motion);
            this.yRot = MathHelper.radToDeg((float) MathHelper.atan2(motionX, motionZ));
            //noinspection StatementWithEmptyBody
            for (this.xRot = MathHelper.radToDeg((float) MathHelper.atan2(motionY, horizontalSpeed)); this.xRot - this.xRotO <
                                                                                                      -180.0f; this.xRotO -= 360.0F) {
            }
            while (this.xRot - this.xRotO >= 180.0F) {
                this.xRotO += 360.0F;
            }
            while (this.yRot - this.yRotO < -180.0f) {
                this.yRotO -= 360.0F;
            }
            while (this.yRot - this.yRotO >= 180.0F) {
                this.yRotO += 360.0F;
            }
            this.xRot = MathHelper.lerp(0.2F, this.xRotO, this.xRot);
            this.yRot = MathHelper.lerp(0.2F, this.yRotO, this.yRot);
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
                    this.level.addParticle(ParticleTypes.BUBBLE,
                                           this.getX() - motionX * 0.25,
                                           this.getY() - motionY * 0.25,
                                           this.getZ() - motionZ * 0.25,
                                           motionX,
                                           motionY,
                                           motionZ);
                }
            }
            double gravity = 0;
            if (!this.isNoGravity()) {
                gravity = Gravity.gravity(this.level.dimensionType());
            }
            motionX -= dragX;
            motionY += -gravity - dragY;
            motionZ -= dragZ;
            this.setPos(this.getX(), this.getY(), this.getZ());
            this.setDeltaMovement(motionX, motionY, motionZ);
            this.checkInsideBlocks();
        }
    }

    protected void tryDespawn() {
        ++this.ticksInGround;
        if (this.ticksInGround >= 1_200) {
            this.remove();
        }
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        Entity shooter = this.getShooter();
        buffer.writeInt(shooter == null ? 0 : shooter.getId());
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
