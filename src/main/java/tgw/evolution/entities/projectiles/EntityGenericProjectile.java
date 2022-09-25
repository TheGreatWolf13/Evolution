package tgw.evolution.entities.projectiles;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.ICollisionBlock;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.network.PacketSCMomentum;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.patches.IPlayerPatch;
import tgw.evolution.util.PlayerHelper;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.earth.Gravity;
import tgw.evolution.util.hitbox.HitboxEntity;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Vec3d;

import java.util.UUID;

public abstract class EntityGenericProjectile<T extends EntityGenericProjectile<T>> extends Entity
        implements IEntityAdditionalSpawnData, IEntityPatch<T> {
    private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(EntityGenericProjectile.class,
                                                                                            EntityDataSerializers.BYTE);
    protected final IntSet hitEntities = new IntOpenHashSet();
    public byte arrowShake;
    public boolean inGround;
    public PickupStatus pickupStatus = PickupStatus.ALLOWED;
    public int ticksInAir;
    public int timeInGround;
    @Nullable
    private LivingEntity cachedOwner;
    private double damage = 2.0;
    @Nullable
    private BlockState inBlockState;
    private double mass = 1;
    @Nullable
    private UUID ownerUUID;
    private int ticksInGround;

    public EntityGenericProjectile(EntityType<? extends EntityGenericProjectile> type,
                                   LivingEntity shooter,
                                   Level level,
                                   double mass) {
        this(type, level);
        this.adjustPos(shooter, MathHelper.fromHand(shooter, shooter.getUsedItemHand()));
        this.setShooter(shooter);
        this.mass = mass;
    }

    public EntityGenericProjectile(EntityType<? extends EntityGenericProjectile> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    protected static float lerpRotation(float oldRot, float newRot) {
        while (newRot - oldRot < -180.0F) {
            oldRot -= 360.0F;
        }
        while (newRot - oldRot >= 180.0F) {
            oldRot += 360.0F;
        }
        return Mth.lerp(0.2F, oldRot, newRot);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putShort("TicksInGround", (short) this.ticksInGround);
        if (this.inBlockState != null) {
            tag.put("BlockStateIn", NbtUtils.writeBlockState(this.inBlockState));
        }
        tag.putByte("Shake", this.arrowShake);
        tag.putBoolean("InGround", this.inGround);
        tag.putByte("Pickup", (byte) this.pickupStatus.ordinal());
        tag.putDouble("Damage", this.damage);
        tag.putDouble("Mass", this.mass);
        tag.putByte("PierceLevel", this.getPierceLevel());
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }
    }

    protected void adjustPos(LivingEntity shooter, HumanoidArm arm) {
        Vec3d pos = MathHelper.getCameraPosition(shooter, 1.0f);
        this.setPos(pos.x(), pos.y(), pos.z());
    }

    public void applyDamageActual(ServerPlayer shooter, float damage, EvolutionDamage.Type type, LivingEntity entity) {
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_ACTUAL.get(type), damage);
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_ACTUAL.get(EvolutionDamage.Type.RANGED), damage);
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_ACTUAL.get(EvolutionDamage.Type.TOTAL), damage);
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT.get(), entity.getType(), damage);
    }

    public void applyDamageRaw(ServerPlayer shooter, float damage, EvolutionDamage.Type type) {
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_RAW.get(type), damage);
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_RAW.get(EvolutionDamage.Type.RANGED), damage);
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_RAW.get(EvolutionDamage.Type.TOTAL), damage);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(PIERCE_LEVEL, (byte) 0);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    protected abstract ItemStack getArrowStack();

    @Override
    public double getBaseMass() {
        return this.mass;
    }

    public double getDamage() {
        return this.damage;
    }

    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions size) {
        return 0.0F;
    }

    @Override
    public float getFrictionModifier() {
        return 0;
    }

    protected SoundEvent getHitBlockSound() {
        return SoundEvents.ARROW_HIT;
    }

    @Override
    public @Nullable HitboxEntity<T> getHitboxes() {
        return null;
    }

    @Override
    public double getLegSlowdown() {
        return 0;
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    public byte getPierceLevel() {
        return this.entityData.get(PIERCE_LEVEL);
    }

    @Nullable
    public LivingEntity getShooter() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        }
        if (this.ownerUUID != null && this.level instanceof ServerLevel server) {
            Entity entity = server.getEntity(this.ownerUUID);
            if (entity instanceof LivingEntity living) {
                this.cachedOwner = living;
                return this.cachedOwner;
            }
        }
        return null;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        this.setDeltaMovement(x, y, z);
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float horizontalLength = MathHelper.sqrt(x * x + z * z);
            this.setXRot((float) MathHelper.atan2Deg(y, horizontalLength));
            this.setYRot((float) MathHelper.atan2Deg(x, z));
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            this.ticksInGround = 0;
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.setPos(x, y, z);
        this.setRot(yaw, pitch);
    }

    protected abstract void onBlockHit(BlockState state);

    protected void onEntityHit(EntityHitResult entityRayTrace) {
        Entity rayTracedEntity = entityRayTrace.getEntity();
        float velocityLength = (float) this.getDeltaMovement().length();
        float damage = (float) (velocityLength * this.damage);
        if (this.getPierceLevel() > 0) {
            //TODO piercing
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
        if (this.isOnFire() && !(rayTracedEntity instanceof EnderMan)) {
            rayTracedEntity.setRemainingFireTicks(5);
        }
        float oldHealth = rayTracedEntity instanceof LivingEntity living ? living.getHealth() : 0;
        if (rayTracedEntity.hurt(source, damage)) {
            if (rayTracedEntity instanceof LivingEntity living) {
                if (shooter instanceof ServerPlayer player) {
                    this.applyDamageRaw(player, damage, source.getType());
                    float actualDamage = oldHealth - living.getHealth();
                    this.applyDamageActual(player, actualDamage, source.getType(), living);
                }
                if (!this.level.isClientSide && this.getPierceLevel() <= 0) {
                    living.setArrowCount(living.getArrowCount() + 1);
                }
                if (!this.level.isClientSide && shooter != null) {
                    EnchantmentHelper.doPostHurtEffects(living, shooter);
                    EnchantmentHelper.doPostDamageEffects(shooter, living);
                }
                if (living != shooter && living instanceof Player && shooter instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }
                if (!rayTracedEntity.isAlive()) {
                    this.hitEntities.add(living.getId());
                }
            }
            this.playSound(this.getHitBlockSound(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0 && !(rayTracedEntity instanceof EnderMan)) {
                this.discard();
            }
        }
        else {
            rayTracedEntity.setRemainingFireTicks(j);
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
            this.setYRot(this.getYRot() + 180.0f);
            this.yRotO += 180.0F;
            this.ticksInAir = 0;
            if (!this.level.isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                if (this.pickupStatus == EntityGenericProjectile.PickupStatus.ALLOWED) {
                    this.spawnAtLocation(this.getArrowStack(), 0.1F);
                }
                this.discard();
            }
        }
    }

    protected void onHit(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();
        if (type == HitResult.Type.ENTITY) {
            this.onEntityHit((EntityHitResult) hitResult);
        }
        else if (type == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos pos = blockHitResult.getBlockPos();
            if (this.onProjectileCollision(this.level.getBlockState(pos), pos)) {
                Vec3 vec3d = blockHitResult.getLocation().subtract(this.position());
                this.setDeltaMovement(vec3d);
                Vec3 vec3d1 = vec3d.normalize().scale(0.05);
                this.setPosRaw(this.getX() - vec3d1.x, this.getY() - vec3d1.y, this.getZ() - vec3d1.z);
                this.playSound(this.getHitBlockSound(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
                this.inBlockState = this.level.getBlockState(blockHitResult.getBlockPos());
                this.inGround = true;
                this.setPierceLevel((byte) 0);
                this.resetHitEntities();
                this.arrowShake = 7;
                this.onBlockHit(this.inBlockState);
            }
        }
    }

    /**
     * @return Whether is speed should be changed internally
     */
    public boolean onProjectileCollision(BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof ICollisionBlock collisionBlock) {
            return !collisionBlock.collision(this.level, pos, this, this.getDeltaMovement().length(), this.mass, null);
        }
        return true;
    }

    @Override
    public void playerTouch(Player player) {
        if (!this.level.isClientSide && this.inGround && this.arrowShake <= 0) {
            boolean canBePickedUp = this.pickupStatus == PickupStatus.ALLOWED ||
                                    this.pickupStatus == PickupStatus.CREATIVE_ONLY && player.getAbilities().instabuild;
            if (this.pickupStatus == PickupStatus.ALLOWED && !player.getInventory().add(this.getArrowStack())) {
                canBePickedUp = false;
            }
            if (canBePickedUp) {
                player.take(this, 1);
                this.playSound(SoundEvents.ITEM_PICKUP, 0.2f, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7f + 1) * 2);
                this.discard();
            }
        }
    }

    @Nullable
    protected EntityHitResult rayTraceEntities(Vec3 startVec, Vec3 endVec) {
        return ProjectileUtil.getEntityHitResult(this.level, this, startVec, endVec,
                                                 this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1),
                                                 entity -> !entity.isSpectator() &&
                                                           entity.isAlive() &&
                                                           entity.isAttackable() &&
                                                           entity.isPickable() &&
                                                           (entity != this.getShooter() || this.ticksInAir >= 5));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        this.ticksInGround = compound.getShort("TicksInGround");
        if (compound.contains("BlockStateIn", Tag.TAG_COMPOUND)) {
            this.inBlockState = NbtUtils.readBlockState(compound.getCompound("BlockStateIn"));
        }
        this.arrowShake = compound.getByte("Shake");
        this.inGround = compound.getBoolean("InGround");
        this.damage = compound.getFloat("Damage");
        this.mass = compound.getDouble("Mass");
        this.pickupStatus = PickupStatus.getByOrdinal(compound.getByte("Pickup"));
        this.setPierceLevel(compound.getByte("PierceLevel"));
        if (compound.hasUUID("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        int id = buffer.readInt();
        if (id != 0) {
            Entity entity = this.level.getEntity(id);
            if (entity instanceof LivingEntity living) {
                this.setShooter(living);
            }
        }
        this.mass = buffer.readDouble();
    }

    private void resetHitEntities() {
        this.hitEntities.clear();
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void setPierceLevel(byte level) {
        this.entityData.set(PIERCE_LEVEL, level);
    }

    public void setShooter(@Nullable LivingEntity entity) {
        if (entity != null) {
            this.ownerUUID = entity.getUUID();
            this.cachedOwner = entity;
            if (entity instanceof Player player) {
                this.pickupStatus = player.getAbilities().instabuild ?
                                    EntityGenericProjectile.PickupStatus.CREATIVE_ONLY :
                                    EntityGenericProjectile.PickupStatus.ALLOWED;
            }
        }
    }

    public void shoot(LivingEntity shooter, float pitch, float yaw, float speed, float inaccuracy) {
        float cosPitch = MathHelper.cosDeg(pitch);
        float x = -MathHelper.sinDeg(yaw) * cosPitch;
        float y = -MathHelper.sinDeg(pitch);
        float z = MathHelper.cosDeg(yaw) * cosPitch;
        this.shoot(x, y, z, speed, inaccuracy);
        Vec3 velocity = this.getDeltaMovement();
        double massRatio = this.mass / shooter.getAttributeValue(EvolutionAttributes.MASS.get());
        double speedX = -velocity.x * massRatio;
        double speedY = -velocity.y * massRatio;
        double speedZ = -velocity.z * massRatio;
        if (shooter instanceof IPlayerPatch player) {
            this.setDeltaMovement(velocity.add(player.getMotionX(), player.getMotionY(), player.getMotionZ()));
        }
        else {
            this.setDeltaMovement(velocity.add(shooter.getDeltaMovement()));
        }
        if (shooter instanceof ServerPlayer player) {
            EvolutionNetwork.send(player, new PacketSCMomentum((float) speedX, (float) speedY, (float) speedZ));
        }
        else {
            shooter.setDeltaMovement(shooter.getDeltaMovement().add(speedX, speedY, speedZ));
        }
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 motion = new Vec3(x, y, z).normalize()
                                       .add(this.random.nextGaussian() * 0.007_5F * inaccuracy, this.random.nextGaussian() * 0.007_5F * inaccuracy,
                                            this.random.nextGaussian() * 0.007_5F * inaccuracy)
                                       .normalize()
                                       .scale(velocity);
        this.setDeltaMovement(motion);
        this.setYRot((float) MathHelper.atan2Deg(motion.x, motion.z));
        this.setXRot((float) MathHelper.atan2Deg(motion.y, motion.horizontalDistance()));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.ticksInGround = 0;
    }

    @Override
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
        Vec3 motion = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            this.setYRot((float) MathHelper.atan2Deg(motion.x, motion.z));
            this.setXRot((float) MathHelper.atan2Deg(motion.y, motion.horizontalDistance()));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
        BlockPos pos = this.blockPosition();
        BlockState stateAtPos = this.level.getBlockState(pos);
        if (!stateAtPos.isAir()) {
            VoxelShape shape = stateAtPos.getCollisionShape(this.level, pos);
            if (!shape.isEmpty()) {
                Vec3 positionVec = this.position();
                for (AABB boundingBox : shape.toAabbs()) {
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
            Vec3 positionVec = this.position();
            Vec3 newPositionVec = positionVec.add(motion);
            HitResult rayTrace = this.level.clip(
                    new ClipContext(positionVec, newPositionVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (rayTrace.getType() != HitResult.Type.MISS) {
                newPositionVec = rayTrace.getLocation();
            }
            while (!this.isRemoved()) {
                EntityHitResult entityRayTrace = this.rayTraceEntities(positionVec, newPositionVec);
                if (entityRayTrace != null) {
                    rayTrace = entityRayTrace;
                }
                if (rayTrace != null && rayTrace.getType() == HitResult.Type.ENTITY) {
                    assert rayTrace instanceof EntityHitResult;
                    Entity rayTracedEntity = ((EntityHitResult) rayTrace).getEntity();
                    Entity shooter = this.getShooter();
                    if (rayTracedEntity instanceof Player hitPlayer &&
                        shooter instanceof Player shooterPlayer &&
                        !shooterPlayer.canAttack(hitPlayer)) {
                        rayTrace = null;
                        entityRayTrace = null;
                    }
                }
                if (rayTrace != null && rayTrace.getType() != HitResult.Type.MISS) {
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
            this.setYRot((float) MathHelper.atan2Deg(motionX, motionZ));
            this.setXRot((float) MathHelper.atan2Deg(motionY, motion.horizontalDistance()));
            this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
            this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
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
                    this.level.addParticle(ParticleTypes.BUBBLE, this.getX() - motionX * 0.25, this.getY() - motionY * 0.25,
                                           this.getZ() - motionZ * 0.25, motionX, motionY, motionZ);
                }
            }
            double gravity = 0;
            if (!this.isNoGravity()) {
                gravity = Gravity.gravity(this.level.dimensionType());
            }
            this.setPos(this.getX() + motionX, this.getY() + motionY, this.getZ() + motionZ);
            motionX -= dragX;
            motionY += -gravity - dragY;
            motionZ -= dragZ;
            this.setDeltaMovement(motionX, motionY, motionZ);
            this.checkInsideBlocks();
        }
    }

    protected void tryDespawn() {
        ++this.ticksInGround;
        if (this.ticksInGround >= 1_200) {
            this.discard();
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        Entity shooter = this.getShooter();
        buffer.writeInt(shooter == null ? 0 : shooter.getId());
        buffer.writeDouble(this.mass);
    }

    public enum PickupStatus {
        DISALLOWED,
        ALLOWED,
        CREATIVE_ONLY;

        public static final PickupStatus[] VALUES = values();

        public static PickupStatus getByOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal > VALUES.length) {
                ordinal = 0;
            }
            return VALUES[ordinal];
        }
    }
}
