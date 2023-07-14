package tgw.evolution.entities.projectiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.ICollisionBlock;
import tgw.evolution.entities.IEntityPacket;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.items.IProjectile;
import tgw.evolution.network.PacketSCCustomEntity;
import tgw.evolution.network.PacketSCMomentum;
import tgw.evolution.patches.PatchPlayer;
import tgw.evolution.util.EntityHelper;
import tgw.evolution.util.MultipleEntityHitResult;
import tgw.evolution.util.PlayerHelper;
import tgw.evolution.util.ProjectileHitInformation;
import tgw.evolution.util.collection.sets.IHashSet;
import tgw.evolution.util.collection.sets.ISet;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.math.ClipContextMutable;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.physics.Fluid;
import tgw.evolution.util.physics.Physics;

import java.util.UUID;

public abstract class EntityGenericProjectile extends Entity implements IEntityPacket<EntityGenericProjectile> {
    private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(EntityGenericProjectile.class,
                                                                                            EntityDataSerializers.BYTE);
    protected final ISet hitEntities = new IHashSet();
    private final ClipContextMutable clipContext = new ClipContextMutable();
    public byte arrowShake;
    public boolean inGround;
    public PickupStatus pickupStatus = PickupStatus.ALLOWED;
    public int ticksInAir;
    public int timeInGround;
    private @Nullable LivingEntity cachedOwner;
    private float damage = 2.0f;
    private int despawnTicks;
    private @Nullable BlockState inBlockState;
    private double mass = 1;
    private @Nullable UUID ownerUUID;

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
        tag.putShort("DespawnTicks", (short) this.despawnTicks);
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
        Vec3 pos = shooter.getEyePosition();
        this.setPos(pos.x(), pos.y(), pos.z());
    }

    protected abstract DamageSourceEv createDamageSource();

    protected abstract boolean damagesEntities();

    @Override
    protected void defineSynchedData() {
        this.entityData.define(PIERCE_LEVEL, (byte) 0);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new PacketSCCustomEntity<>(this);
    }

    protected abstract ItemStack getArrowStack();

    @Override
    public double getBaseMass() {
        return this.mass;
    }

    public float getDamage(@Nullable IProjectile throwable, Entity hitEntity) {
        if (throwable != null && throwable.isDamageProportionalToMomentum()) {
            double relativeSpeedX = this.getDeltaMovement().x - hitEntity.getDeltaMovement().x;
            double relativeSpeedY = this.getDeltaMovement().y - hitEntity.getDeltaMovement().y;
            double relativeSpeedZ = this.getDeltaMovement().z - hitEntity.getDeltaMovement().z;
            float relativeVelocitySqr = (float) (relativeSpeedX * relativeSpeedX + relativeSpeedY * relativeSpeedY + relativeSpeedZ * relativeSpeedZ);
            double throwSpeed = throwable.projectileSpeed();
            return (float) (this.damage * relativeVelocitySqr / (throwSpeed * throwSpeed));
        }
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
    public @Nullable HitboxEntity<? extends EntityGenericProjectile> getHitboxes() {
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

    protected abstract @Nullable IProjectile getProjectile();

    public @Nullable LivingEntity getShooter() {
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
            this.despawnTicks = 0;
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.setPos(x, y, z);
        this.setRot(yaw, pitch);
    }

    protected abstract void modifyMovementOnCollision();

    protected abstract void onBlockHit(BlockState state);

    protected final boolean onEntityHit(MultipleEntityHitResult hitResult) {
        if (this.damagesEntities()) {
            ProjectileHitInformation hits = null;
            int entityHits = 0;
            boolean hitSomething = false;
            allEntities:
            for (Entity hitEntity = hitResult.popEntity(); hitEntity != null; hitEntity = hitResult.popEntity()) {
                if (this.hitEntities.contains(hitEntity.getId())) {
                    continue;
                }
                if (hits == null) {
                    hits = new ProjectileHitInformation();
                    hits.prepare(hitResult.getStart(), hitResult.getEnd(), Mth.SQRT_OF_TWO * this.getBbWidth());
                }
                else {
                    hits.clear();
                }
                for (float partialTicks = 0; partialTicks <= 1.0f; partialTicks += 0.25f) {
                    MathHelper.collideOBBWithProjectile(hits, partialTicks, hitEntity);
                    if (!hits.isEmpty()) {
                        LivingEntity shooter = this.getShooter();
                        boolean attackSuccessful = false;
                        if (hitEntity instanceof LivingEntity living && hitEntity.isAttackable()) {
                            float damage = this.getDamage(this.getProjectile(), hitEntity);
                            DamageSourceEv source = this.createDamageSource();
                            attackSuccessful = EntityHelper.hurt(hitEntity, source, damage, hits.getHitboxSet());
                            if (attackSuccessful && shooter instanceof ServerPlayer shooterPlayer) {
                                this.recordDamageStats(shooterPlayer, damage, source.getType(), living);
                            }
                        }
                        if (entityHits == 0) {
                            this.modifyMovementOnCollision();
                            this.playHitEntitySound();
                        }
                        this.postHitLogic(attackSuccessful);
                        if (attackSuccessful) {
                            hitSomething = true;
                            this.hitEntities.add(hitEntity.getId());
                            if (entityHits++ >= this.getPierceLevel()) {
                                return true;
                            }
                            continue allEntities;
                        }
                    }
                }
            }
            return hitSomething;
        }
        return this.onHitEntityLogic();
    }

    protected boolean onHit(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();
        if (hitResult instanceof MultipleEntityHitResult hr) {
            return this.onEntityHit(hr);
        }
        if (type == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos pos = blockHitResult.getBlockPos();
            if (this.onProjectileCollision(this.level.getBlockState_(pos.getX(), pos.getY(), pos.getZ()), pos.getX(), pos.getY(), pos.getZ())) {
                Vec3 vec3d = blockHitResult.getLocation().subtract(this.position());
                this.setDeltaMovement(vec3d);
                Vec3 vec3d1 = vec3d.normalize().scale(0.05);
                this.setPosRaw(this.getX() - vec3d1.x, this.getY() - vec3d1.y, this.getZ() - vec3d1.z);
                this.playSound(this.getHitBlockSound(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
                this.inGround = true;
                this.setPierceLevel((byte) 0);
                this.resetHitEntities();
                this.arrowShake = 7;
                this.inBlockState = this.level.getBlockState_(pos.getX(), pos.getY(), pos.getZ());
                this.onBlockHit(this.inBlockState);
            }
        }
        return true;
    }

    protected boolean onHitEntityLogic() {
        return true;
    }

    /**
     * @return Whether is speed should be changed internally
     */
    public boolean onProjectileCollision(BlockState state, int x, int y, int z) {
        if (state.getBlock() instanceof ICollisionBlock collisionBlock) {
            return !collisionBlock.collision(this.level, x, y, z, this, this.getDeltaMovement().length(), this.mass, null);
        }
        return true;
    }

    protected abstract void playHitEntitySound();

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

    protected abstract void postHitLogic(boolean attackSuccessful);

    protected @Nullable MultipleEntityHitResult rayTraceEntities(Vec3 startVec, Vec3 endVec) {
        return MathHelper.getProjectileHitResult(this.level, this, startVec, endVec, e -> !e.isSpectator() &&
                                                                                          e.isAlive() &&
                                                                                          e.isAttackable() &&
                                                                                          e.isPickable() &&
                                                                                          (e != this.getShooter() || this.ticksInAir >= 5), 1);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        this.despawnTicks = compound.getShort("TicksInGround");
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
    public void readAdditionalSyncData(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        if (id != 0) {
            Entity entity = this.level.getEntity(id);
            if (entity instanceof LivingEntity living) {
                this.setShooter(living);
            }
        }
        this.mass = buf.readFloat();
    }

    public void recordDamageStats(ServerPlayer shooter, float damage, EvolutionDamage.Type type, LivingEntity entity) {
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_BY_TYPE.get(type), damage);
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_BY_TYPE.get(EvolutionDamage.Type.RANGED), damage);
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT_BY_TYPE.get(EvolutionDamage.Type.TOTAL), damage);
        PlayerHelper.addStat(shooter, EvolutionStats.DAMAGE_DEALT, entity.getType(), damage);
    }

    private void resetHitEntities() {
        this.hitEntities.clear();
    }

    public void setDamage(float damage) {
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
                this.pickupStatus = player.getAbilities().instabuild ? PickupStatus.CREATIVE_ONLY : PickupStatus.ALLOWED;
            }
        }
    }

    public void shoot(LivingEntity shooter, float pitch, float yaw, IProjectile throwable) {
        float stdDev = 1 - throwable.precision();
        pitch += 12 * MathHelper.clamp(this.random.nextGaussian(), -3, 3) * stdDev;
        yaw += 12 * MathHelper.clamp(this.random.nextGaussian(), -3, 3) * stdDev;
        float cosPitch = MathHelper.cosDeg(pitch);
        float x = -MathHelper.sinDeg(yaw) * cosPitch;
        float y = -MathHelper.sinDeg(pitch);
        float z = MathHelper.cosDeg(yaw) * cosPitch;
        this.shoot(x, y, z, throwable);
        Vec3 velocity = this.getDeltaMovement();
        double massRatio = this.mass / shooter.getAttributeValue(EvolutionAttributes.MASS);
        double speedX = -velocity.x * massRatio;
        double speedY = -velocity.y * massRatio;
        double speedZ = -velocity.z * massRatio;
        if (shooter instanceof PatchPlayer player) {
            this.setDeltaMovement(velocity.x + player.getMotionX(), velocity.y + player.getMotionY(), velocity.z + player.getMotionZ());
        }
        else {
            Vec3 shooterVelocity = shooter.getDeltaMovement();
            this.setDeltaMovement(velocity.x + shooterVelocity.x, velocity.y + shooterVelocity.y, velocity.z + shooterVelocity.z);
        }
        if (shooter instanceof ServerPlayer player) {
            player.connection.send(new PacketSCMomentum((float) speedX, (float) speedY, (float) speedZ));
        }
        else {
            Vec3 shooterVelocity = shooter.getDeltaMovement();
            shooter.setDeltaMovement(shooterVelocity.x + speedX, shooterVelocity.y + speedY, shooterVelocity.z + speedZ);
        }
    }

    public void shoot(double x, double y, double z, IProjectile throwable) {
        Vec3d velocity = new Vec3d(x, y, z).normalizeMutable().scaleMutable(throwable.projectileSpeed());
        this.setDeltaMovement(velocity);
        this.setYRot((float) MathHelper.atan2Deg(velocity.x, velocity.z));
        this.setXRot((float) MathHelper.atan2Deg(velocity.y, velocity.horizontalDistance()));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.despawnTicks = 0;
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
        BlockState stateAtPos = this.level.getBlockState_(pos.getX(), pos.getY(), pos.getZ());
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
                this.despawnTicks = 0;
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
            BlockHitResult blockHitResult = this.level.clip(
                    this.clipContext.set(positionVec, newPositionVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                newPositionVec = blockHitResult.getLocation();
            }
            HitResult hitResult = blockHitResult;
            if (!this.level.isClientSide) {
                MultipleEntityHitResult entityRayTrace = this.rayTraceEntities(positionVec, newPositionVec);
                if (entityRayTrace != null) {
                    hitResult = entityRayTrace;
                }
            }
            if (hitResult.getType() != HitResult.Type.MISS) {
                if (!this.onHit(hitResult)) {
                    this.onHit(blockHitResult);
                }
                this.hasImpulse = true;
            }
            motion = this.getDeltaMovement();
            double motionX = motion.x;
            double motionY = motion.y;
            double motionZ = motion.z;
            this.setYRot((float) MathHelper.atan2Deg(motionX, motionZ));
            this.setXRot((float) MathHelper.atan2Deg(motionY, motion.horizontalDistance()));
            this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
            this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
            try (Physics physics = Physics.getInstance(this, this.isInWater() ? Fluid.WATER : this.isInLava() ? Fluid.LAVA : Fluid.AIR)) {
                double accY = 0;
                if (!this.isNoGravity()) {
                    accY += physics.calcAccGravity();
                }
                if (!this.isOnGround()) {
                    accY += physics.calcForceBuoyancy(this) / this.mass;
                }
                //Pseudo-forces
                double accCoriolisX = physics.calcAccCoriolisX();
                double accCoriolisY = physics.calcAccCoriolisY();
                double accCoriolisZ = physics.calcAccCoriolisZ();
                double accCentrifugalY = physics.calcAccCentrifugalY();
                double accCentrifugalZ = physics.calcAccCentrifugalZ();
                //Drag
                //TODO wind speed
//                double windVelX = 0;
//                double windVelY = 0;
//                double windVelZ = 0;
//                double dragX = physics.calcForceDragX(windVelX) / this.mass;
//                double dragY = physics.calcForceDragY(windVelY) / this.mass;
//                double dragZ = physics.calcForceDragZ(windVelZ) / this.mass;
//                double maxDrag = Math.abs(windVelX - motionX);
//                if (Math.abs(dragX) > maxDrag) {
//                    dragX = Math.signum(dragX) * maxDrag;
//                }
//                maxDrag = Math.abs(windVelY - motionY);
//                if (Math.abs(dragY) > maxDrag) {
//                    dragY = Math.signum(dragY) * maxDrag;
//                }
//                maxDrag = Math.abs(windVelZ - motionZ);
//                if (Math.abs(dragZ) > maxDrag) {
//                    dragZ = Math.signum(dragZ) * maxDrag;
//                }
                if (this.isInWater()) {
                    for (int j = 0; j < 4; ++j) {
                        this.level.addParticle(ParticleTypes.BUBBLE, this.getX() - motionX * 0.25, this.getY() - motionY * 0.25,
                                               this.getZ() - motionZ * 0.25, motionX, motionY, motionZ);
                    }
                }
                this.setPos(this.getX() + motionX, this.getY() + motionY, this.getZ() + motionZ);
                //Update Motion
                motionX += /*dragX +*/ accCoriolisX;
                motionY += accY + /*dragY +*/ accCoriolisY + accCentrifugalY;
                motionZ += /*dragZ +*/ accCoriolisZ + accCentrifugalZ;
            }
            this.setDeltaMovement(motionX, motionY, motionZ);
            this.checkInsideBlocks();
        }
    }

    protected void tryDespawn() {
        ++this.despawnTicks;
        if (this.despawnTicks >= 1_200) {
            this.discard();
        }
    }

    @Override
    public void writeAdditionalSyncData(FriendlyByteBuf buf) {
        Entity shooter = this.getShooter();
        buf.writeVarInt(shooter == null ? 0 : shooter.getId());
        buf.writeFloat((float) this.mass);
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
