package tgw.evolution.entities;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import tgw.evolution.util.EntityFlags;
import tgw.evolution.util.MathHelper;

public abstract class CreatureEntity extends net.minecraft.entity.CreatureEntity implements IEntityMass {

    protected static final DataParameter<Boolean> DEAD = EntityDataManager.createKey(CreatureEntity.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Boolean> SKELETON = EntityDataManager.createKey(CreatureEntity.class, DataSerializers.BOOLEAN);
    protected int deathTimer;
    private int jumpTicks;

    protected CreatureEntity(EntityType<? extends CreatureEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    public boolean canBeAttackedWithItem() {
        return !this.dataManager.get(DEAD);
    }

    /**
     * Gets the time the entity has been dead for, in ticks.
     */
    public int getDeathTime() {
        return this.deathTimer;
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.getBaseHealth());
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.getBaseMovementSpeed());
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(DEAD, false);
        this.dataManager.register(SKELETON, false);
    }

    @Override
    protected int getExperiencePoints(PlayerEntity player) {
        return 0;
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("DeathTimer", this.deathTimer);
        compound.putBoolean("Dead", this.dataManager.get(DEAD));
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.deathTimer = compound.getInt("DeathTimer");
        this.dataManager.set(DEAD, compound.getBoolean("Dead"));
    }

    @Override
    public void livingTick() {
        //Start of livingTick from LivingEntity
        if (this.jumpTicks > 0) {
            --this.jumpTicks;
        }
        if (this.newPosRotationIncrements > 0 && !this.canPassengerSteer()) {
            double newX = this.posX + (this.interpTargetX - this.posX) / (double) this.newPosRotationIncrements;
            double newY = this.posY + (this.interpTargetY - this.posY) / (double) this.newPosRotationIncrements;
            double newZ = this.posZ + (this.interpTargetZ - this.posZ) / (double) this.newPosRotationIncrements;
            double newYaw = MathHelper.wrapDegrees(this.interpTargetYaw - (double) this.rotationYaw);
            this.rotationYaw = (float) ((double) this.rotationYaw + newYaw / (double) this.newPosRotationIncrements);
            this.rotationPitch = (float) ((double) this.rotationPitch +
                                          (this.interpTargetPitch - (double) this.rotationPitch) / (double) this.newPosRotationIncrements);
            --this.newPosRotationIncrements;
            this.setPosition(newX, newY, newZ);
            this.setRotation(this.rotationYaw, this.rotationPitch);
        }
        //Applies simple air resistance on the client only
//        else if (!this.isServerWorld()) {
//            this.setMotion(this.getMotion().scale(0.98D));
//        }
        if (this.interpTicksHead > 0) {
            this.rotationYawHead = (float) ((double) this.rotationYawHead +
                                            MathHelper.wrapDegrees(this.interpTargetHeadYaw - (double) this.rotationYawHead) /
                                            (double) this.interpTicksHead);
            --this.interpTicksHead;
        }
        //When velocities are very low (< 0.003) changes to 0
        Vec3d motion = this.getMotion();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        boolean changed = false;
        if (Math.abs(motion.x) < 0.003) {
            motionX = 0.0D;
            changed = true;
        }
        if (Math.abs(motion.y) < 0.003) {
            motionY = 0.0D;
            changed = true;
        }
        if (Math.abs(motion.z) < 0.003) {
            motionZ = 0.0D;
            changed = true;
        }
        if (changed) {
            this.setMotion(motionX, motionY, motionZ);
        }
        this.world.getProfiler().startSection("ai");
        if (this.isMovementBlocked()) {
            this.isJumping = false;
            this.moveStrafing = 0F;
            this.moveForward = 0F;
            this.randomYawVelocity = 0F;
        }
        else if (this.isServerWorld()) {
            this.world.getProfiler().startSection("newAi");
            this.updateEntityActionState();
            this.world.getProfiler().endSection();
        }
        this.world.getProfiler().endSection();
        this.world.getProfiler().startSection("jump");
        if (this.isJumping) {
            if (!(this.submergedHeight > 0) || this.onGround && !(this.submergedHeight > 0.4)) {
                if (this.isInLava()) {
                    this.handleFluidJump(FluidTags.LAVA);
                }
                else if ((this.onGround || this.submergedHeight > 0 && this.submergedHeight <= 0.4) && this.jumpTicks == 0) {
                    this.jump();
                    this.jumpTicks = 10;
                }
            }
            else {
                this.handleFluidJump(FluidTags.WATER);
            }
        }
        else {
            this.jumpTicks = 0;
        }
        this.world.getProfiler().endSection();
        this.world.getProfiler().startSection("travel");
        //reducing movement
//        this.moveStrafing *= 0.98F;
//        this.moveForward *= 0.98F;
        this.randomYawVelocity *= 0.9F;
//        this.updateElytra();
//        AxisAlignedBB boundingBox = this.getBoundingBox();
        this.travel(new Vec3d(this.moveStrafing, this.moveVertical, this.moveForward));
        this.world.getProfiler().endSection();
        this.world.getProfiler().startSection("push");
//        if (this.spinAttackDuration > 0) {
//            --this.spinAttackDuration;
//            this.updateSpinAttack(boundingBox, this.getBoundingBox());
//        }
        this.collideWithNearbyEntities();
        this.world.getProfiler().endSection();
        //End of livingTick from LivingEntity
        //Start of livingTick from MobEntity
        this.world.getProfiler().startSection("looting");
        if (!this.world.isRemote && this.canPickUpLoot() && !this.isDead() && ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
            for (ItemEntity item : this.world.getEntitiesWithinAABB(ItemEntity.class, this.getBoundingBox().grow(1.0D, 0.0D, 1.0D))) {
                if (!item.removed && !item.getItem().isEmpty() && !item.cannotPickup()) {
                    this.updateEquipmentIfNeeded(item);
                }
            }
        }
        this.world.getProfiler().endSection();
        //End of livingTick from MobEntity
        //Start of my livingTick
        if (this.isDead()) {
            this.deathTimer++;
            if (this.deathTimer == 1) {
                this.setMotion(0, this.getMotion().y, 0);
                this.navigator.clearPath();
            }
            if (!this.isSkeleton() && this.skeletonTime() > 0) {
                if (this.deathTimer >= this.skeletonTime()) {
                    if (this.becomesSkeleton()) {
                        this.dataManager.set(SKELETON, true);
                    }
                    else {
                        this.remove();
                    }
                }
            }
        }
    }

    /**
     * @return Whether this entity is in the 'dead state'.
     */
    public boolean isDead() {
        return this.dataManager.get(DEAD);
    }

    public boolean isSkeleton() {
        return this.dataManager.get(SKELETON);
    }

    /**
     * @return The time it takes for the entity's body to become a 'skeleton', in ticks.
     * If {@code 0}, the entity does not turn into skeleton.
     */
    public abstract int skeletonTime();

    /**
     * @return Whether this entity becomes a skeleton after {@link CreatureEntity#skeletonTime()} ticks or simply disappears.
     */
    public abstract boolean becomesSkeleton();

    private float jumpMovementFactor(float slipperiness) {
        return this.onGround ? this.getAIMoveSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : this.jumpMovementFactor;
    }

    private Vec3d handleLadderMotion(Vec3d motion) {
        if (this.isOnLadder()) {
            this.fallDistance = 0F;
            double motionX = MathHelper.clamp(motion.x, -0.15F, 0.15F);
            double motionZ = MathHelper.clamp(motion.z, -0.15F, 0.15F);
            double motionY = Math.max(motion.y, -0.15F);
            return new Vec3d(motionX, motionY, motionZ);
        }
        return motion;
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean preventDespawn() {
        return true;
    }

    @Override
    protected void handleFluidJump(Tag<Fluid> fluidTag) {
        if (this.getNavigator().getCanSwim()) {
            super.handleFluidJump(fluidTag);
        }
//        else {
//            this.setMotion(this.getMotion().add(0.0D, 0.3D, 0.0D));
//        }
    }

    public abstract float getBaseHealth();

//    private void updateElytra() {
//        boolean isElytraFlying = this.getFlag(EntityFlags.ELYTRA_FLYING);
//        if (isElytraFlying && !this.onGround && !this.isPassenger()) {
//            ItemStack chestItem = this.getItemStackFromSlot(EquipmentSlotType.CHEST);
//            if (chestItem.getItem() == Items.ELYTRA && ElytraItem.isUsable(chestItem)) {
//                if (!this.world.isRemote && (this.ticksElytraFlying + 1) % 20 == 0) {
//                    chestItem.damageItem(1, this, entity -> entity.sendBreakAnimation(EquipmentSlotType.CHEST));
//                }
//            }
//            else {
//                isElytraFlying = false;
//            }
//        }
//        else {
//            isElytraFlying = false;
//        }
//        if (!this.world.isRemote) {
//            this.setFlag(EntityFlags.ELYTRA_FLYING, isElytraFlying);
//        }
//    }

    public abstract float getBaseMovementSpeed();

    /**
     * @return The leg height of the entity in m.
     */
    public abstract double getLegHeight();

    @Override
    public void onDeath(DamageSource cause) {
        if (ForgeHooks.onLivingDeath(this, cause)) {
            return;
        }
        if (cause == DamageSource.OUT_OF_WORLD) {
            if (!this.dead) {
                Entity trueSource = cause.getTrueSource();
                if (trueSource != null) {
                    trueSource.onKillEntity(this);
                }
                if (this.isSleeping()) {
                    this.wakeUp();
                }
                this.dead = true;
                this.getCombatTracker().reset();
                this.world.setEntityState(this, (byte) 3);
                this.spawnExplosionParticle();
                this.setPose(Pose.DYING);
            }
            return;
        }
        if (!this.isDead()) {
            Entity trueSource = cause.getTrueSource();
            if (trueSource != null) {
                trueSource.onKillEntity(this);
            }
            this.getCombatTracker().reset();
            this.world.setEntityState(this, (byte) 3);
            this.kill();
        }
    }

    /**
     * Kills this entity.
     */
    public void kill() {
        this.dataManager.set(DEAD, true);
        this.setInvulnerable(true);
        this.setHealth(this.getMaxHealth());
        this.setPose(Pose.DYING);
        this.spawnExplosionParticle();
    }

    @Override
    protected float getJumpUpwardsMotion() {
        //TODO
        return 0.42F;
    }

    @Override
    protected void jump() {
        float upwardMotion;
        if (this.isPotionActive(Effects.JUMP_BOOST)) {
            upwardMotion = this.getJumpUpwardsMotion() + 0.1F * (float) (this.getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1);
        }
        else {
            upwardMotion = this.getJumpUpwardsMotion();
        }
        Vec3d motion = this.getMotion();
        this.setMotion(motion.x, upwardMotion, motion.z);
        //Entity loses momentum when jumping and sprinting
//        if (this.isSprinting()) {
//            float rotationYawInRadians = this.rotationYaw * (MathHelper.PI / 180F);
//            this.setMotion(this.getMotion().add(-MathHelper.sin(rotationYawInRadians) * 0.2F, 0.0D, MathHelper.cos(rotationYawInRadians) * 0.2F));
//        }
        this.isAirBorne = true;
        ForgeHooks.onLivingJump(this);
    }

    @Override
    public void travel(Vec3d motion) {
        if (this.isServerWorld() || this.canPassengerSteer()) {
            IAttributeInstance gravity = this.getAttribute(ENTITY_GRAVITY);
            boolean falling = this.getMotion().y <= 0;
            if (falling && this.isPotionActive(Effects.SLOW_FALLING)) {
                if (!gravity.hasModifier(EvolutionAttributes.SLOW_FALLING)) {
                    gravity.applyModifier(EvolutionAttributes.SLOW_FALLING);
                }
                this.fallDistance = 0F;
            }
            else if (gravity.hasModifier(EvolutionAttributes.SLOW_FALLING)) {
                gravity.removeModifier(EvolutionAttributes.SLOW_FALLING);
            }
            double gravityAcceleration = gravity.getValue();
            if (!this.isInWater()) {
                if (!this.isInLava()) {
                    if (this.isElytraFlying()) {
                        Vec3d currentMotion = this.getMotion();
                        if (currentMotion.y > -0.5) {
                            this.fallDistance = 1F;
                        }
                        Vec3d lookVec = this.getLookVec();
                        float rotationPitchInRadians = MathHelper.degToRad(this.rotationPitch);
                        double horizontalLookVecNorm = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
                        double horizontalMotionNorm = Math.sqrt(horizontalMag(currentMotion));
                        float cosRotationPitch = MathHelper.cos(rotationPitchInRadians);
                        cosRotationPitch = (float) (cosRotationPitch * cosRotationPitch * Math.min(1, lookVec.length() / 0.4));
                        currentMotion = this.getMotion().add(0, gravityAcceleration * (-1 + cosRotationPitch * 0.75), 0);
                        if (currentMotion.y < 0 && horizontalLookVecNorm > 0) {
                            double d3 = currentMotion.y * -0.1 * cosRotationPitch;
                            currentMotion = currentMotion.add(lookVec.x * d3 / horizontalLookVecNorm, d3, lookVec.z * d3 / horizontalLookVecNorm);
                        }
                        if (rotationPitchInRadians < 0F && horizontalLookVecNorm > 0) {
                            double d13 = horizontalMotionNorm * -MathHelper.sin(rotationPitchInRadians) * 0.04;
                            currentMotion = currentMotion.add(-lookVec.x * d13 / horizontalLookVecNorm,
                                                              d13 * 3.2,
                                                              -lookVec.z * d13 / horizontalLookVecNorm);
                        }
                        if (horizontalLookVecNorm > 0.0D) {
                            currentMotion = currentMotion.add((lookVec.x / horizontalLookVecNorm * horizontalMotionNorm - currentMotion.x) * 0.1D,
                                                              0.0D,
                                                              (lookVec.z / horizontalLookVecNorm * horizontalMotionNorm - currentMotion.z) * 0.1D);
                        }
                        //TODO apply drag
//                        this.setMotion(currentMotion.mul(0.99F, 0.98F, 0.99F));
                        this.move(MoverType.SELF, this.getMotion());
                        if (this.collidedHorizontally && !this.world.isRemote) {
                            double d14 = Math.sqrt(horizontalMag(this.getMotion()));
                            double d4 = horizontalMotionNorm - d14;
                            float f4 = (float) (d4 * 10 - 3);
                            if (f4 > 0F) {
                                this.playSound(this.getFallSound((int) f4), 1F, 1F);
                                this.attackEntityFrom(DamageSource.FLY_INTO_WALL, f4);
                            }
                        }
                        if (this.onGround && !this.world.isRemote) {
                            this.setFlag(EntityFlags.ELYTRA_FLYING, false);
                        }
                    }
                    else {
                        BlockPos blockBelow = new BlockPos(this.posX, this.getBoundingBox().minY - 1, this.posZ);
                        float slipperiness = this.world.getBlockState(blockBelow).getSlipperiness(this.world, blockBelow, this);
                        //Modification
                        float frictionCoef = this.onGround ? 1F - slipperiness : 0F;
                        this.moveRelative(this.jumpMovementFactor(slipperiness), motion);
                        this.setMotion(this.handleLadderMotion(this.getMotion()));
                        this.move(MoverType.SELF, this.getMotion());
                        Vec3d currentMotion = this.getMotion();
                        if ((this.collidedHorizontally || this.isJumping) && this.isOnLadder()) {
                            currentMotion = new Vec3d(currentMotion.x, 0.2, currentMotion.z);
                        }
                        double motionY = currentMotion.y;
                        if (this.isPotionActive(Effects.LEVITATION)) {
                            motionY += (0.05 * (this.getActivePotionEffect(Effects.LEVITATION).getAmplifier() + 1) - currentMotion.y) * 0.2;
                            this.fallDistance = 0F;
                        }
                        else if (!this.hasNoGravity()) {
                            motionY -= gravityAcceleration;
                        }
                        //TODO Apply friction and drag
                        this.setMotion(currentMotion.x + Math.signum(-currentMotion.x) * frictionCoef * gravityAcceleration,
                                       motionY,
                                       currentMotion.z + Math.signum(-currentMotion.z) * frictionCoef * gravityAcceleration);
                    }
                }
                else {
                    //Is in lava
                    double currentYPos = this.posY;
                    this.moveRelative(0.02F, motion);
                    this.move(MoverType.SELF, this.getMotion());
                    this.setMotion(this.getMotion().scale(0.5));
                    if (!this.hasNoGravity()) {
                        this.setMotion(this.getMotion().add(0, -gravityAcceleration / 4, 0));
                    }
                    Vec3d currentMotion = this.getMotion();
                    if (this.collidedHorizontally &&
                        this.isOffsetPositionInLiquid(currentMotion.x, currentMotion.y + 0.6 - this.posY + currentYPos, currentMotion.z)) {
                        this.setMotion(currentMotion.x, 0.3, currentMotion.z);
                    }
                }
            }
            else {
                //Is in water
                double currentYPos = this.posY;
                float waterSpeed = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
                float depthStriderModifier = EnchantmentHelper.getDepthStriderModifier(this);
                if (depthStriderModifier > 3.0F) {
                    depthStriderModifier = 3.0F;
                }
                if (!this.onGround) {
                    depthStriderModifier *= 0.5F;
                }
                float waterSpeedMult = 0.02F;
                if (depthStriderModifier > 0.0F) {
                    waterSpeed += (0.54600006F - waterSpeed) * depthStriderModifier / 3.0F;
                    waterSpeedMult += (this.getAIMoveSpeed() - waterSpeedMult) * depthStriderModifier / 3.0F;
                }
                if (this.isPotionActive(Effects.DOLPHINS_GRACE)) {
                    waterSpeed = 0.96F;
                }
                waterSpeedMult *= (float) this.getAttribute(SWIM_SPEED).getValue();
                this.moveRelative(waterSpeedMult, motion);
                this.move(MoverType.SELF, this.getMotion());
                Vec3d currentMotion = this.getMotion();
                if (this.collidedHorizontally && this.isOnLadder()) {
                    currentMotion = new Vec3d(currentMotion.x, 0.2, currentMotion.z);
                }
                this.setMotion(currentMotion.mul(waterSpeed, 0.8, waterSpeed));
                if (!this.hasNoGravity() && !this.isSprinting()) {
                    Vec3d motionNotSprinting = this.getMotion();
                    double newYMotion;
                    if (falling &&
                        Math.abs(motionNotSprinting.y - 0.005) >= 0.003 &&
                        Math.abs(motionNotSprinting.y - gravityAcceleration / 16) < 0.003) {
                        newYMotion = -0.003;
                    }
                    else {
                        newYMotion = motionNotSprinting.y - gravityAcceleration / 16.0D;
                    }
                    this.setMotion(motionNotSprinting.x, newYMotion, motionNotSprinting.z);
                }
                currentMotion = this.getMotion();
                if (this.collidedHorizontally &&
                    this.isOffsetPositionInLiquid(currentMotion.x, currentMotion.y + 0.6 - this.posY + currentYPos, currentMotion.z)) {
                    this.setMotion(currentMotion.x, 0.3, currentMotion.z);
                }
            }
        }
        this.prevLimbSwingAmount = this.limbSwingAmount;
        double deltaPosX = this.posX - this.prevPosX;
        double deltaPosZ = this.posZ - this.prevPosZ;
        double deltaPosY = this instanceof IFlyingAnimal ? this.posY - this.prevPosY : 0;
        float f8 = MathHelper.sqrt(deltaPosX * deltaPosX + deltaPosY * deltaPosY + deltaPosZ * deltaPosZ) * 4F;
        if (f8 > 1.0F) {
            f8 = 1.0F;
        }
        this.limbSwingAmount += (f8 - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    @Override
    public Direction getBedDirection() {
        return Direction.UP;
    }
}
