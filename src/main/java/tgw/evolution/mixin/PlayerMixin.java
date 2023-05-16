package tgw.evolution.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.Evolution;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.network.PacketCSPlayerFall;
import tgw.evolution.network.PacketSCHitmarker;
import tgw.evolution.network.PacketSCMovement;
import tgw.evolution.patches.IPlayerPatch;
import tgw.evolution.util.PlayerHelper;
import tgw.evolution.util.constants.SkinType;
import tgw.evolution.util.damage.DamageSourceEntity;
import tgw.evolution.util.damage.DamageSourceEntityIndirect;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.hitbox.EvolutionEntityHitboxes;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.physics.SI;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements IPlayerPatch {

    @Shadow
    @Final
    private Abilities abilities;
    @Shadow
    @Nullable
    private Pose forcedPose;
    private boolean isCrawling;
    private boolean isMoving;
    private double motionX;
    private double motionY;
    private double motionZ;

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    /**
     * @author The Great Wolf
     * @reason Handle Evolution damage system and stats.
     */
    @Override
    @Overwrite
    protected void actuallyHurt(DamageSource source, float amount) {
        if (!this.isInvulnerableTo(source)) {
            if (amount <= 0) {
                return;
            }
            float damageAfterAbsorp = Math.max(amount - this.getAbsorptionAmount(), 0.0F);
            float damageAbsorp = amount - damageAfterAbsorp;
            this.setAbsorptionAmount(this.getAbsorptionAmount() - damageAbsorp);
            if (damageAfterAbsorp != 0.0F) {
                float oldHealth = this.getHealth();
                this.getCombatTracker().recordDamage(source, oldHealth, damageAfterAbsorp);
                this.setHealth(oldHealth - damageAfterAbsorp);
                this.gameEvent(GameEvent.ENTITY_DAMAGED, source.getEntity());
                //noinspection ConstantConditions
                if ((Object) this instanceof ServerPlayer player) {
                    if (source instanceof DamageSourceEv sourceEv) {
                        EvolutionDamage.Type damageType = sourceEv.getType();
                        ResourceLocation resLoc = EvolutionStats.DAMAGE_TAKEN_BY_TYPE.get(damageType);
                        if (resLoc != null) {
                            PlayerHelper.addStat(player, resLoc, damageAfterAbsorp);
                        }
                        if (source instanceof DamageSourceEntity) {
                            if (source instanceof DamageSourceEntityIndirect) {
                                PlayerHelper.addStat(player, EvolutionStats.DAMAGE_TAKEN_BY_TYPE.get(EvolutionDamage.Type.RANGED), damageAfterAbsorp);
                            }
                            else {
                                PlayerHelper.addStat(player, EvolutionStats.DAMAGE_TAKEN_BY_TYPE.get(EvolutionDamage.Type.MELEE), damageAfterAbsorp);
                            }
                        }
                        PlayerHelper.addStat(player, EvolutionStats.DAMAGE_TAKEN_BY_TYPE.get(EvolutionDamage.Type.TOTAL), damageAfterAbsorp);
                    }
                    else {
                        Evolution.warn("Bad damage source: " + source);
                    }
                }
            }
            if (source.getEntity() instanceof ServerPlayer sourcePlayer) {
                EvolutionNetwork.send(sourcePlayer, new PacketSCHitmarker(false));
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Make fall damage depend on kinetic energy, not fall distance.
     */
    @Override
    @Overwrite
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        if (this.abilities.mayfly) {
            if (fallDistance > 1 / 16.0f) {
                this.playBlockFallSound();
            }
            return false;
        }
        if (this.level.isClientSide) {
            EvolutionNetwork.sendToServer(new PacketCSPlayerFall(this.getDeltaMovement().y, 1 - multiplier));
            return super.causeFallDamage(fallDistance, multiplier, source);
        }
        return false;
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to use Evolution Stats.
     */
    @Overwrite
    public void checkMovementStatistics(double dx, double dy, double dz) {
        this.isMoving = false;
        this.motionX = dx;
        this.motionY = dy;
        this.motionZ = dz;
        //noinspection ConstantConditions
        if (!this.isPassenger() && (Object) this instanceof ServerPlayer player) {
            float dist = MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * 1_000;
            if (dist > 0) {
                PlayerHelper.addStat(player, EvolutionStats.TOTAL_DISTANCE_TRAVELED, dist);
            }
            if (this.isSwimming()) {
                if (dist > 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_SWUM, dist);
                }
            }
            else if (this.isEyeInFluid(FluidTags.WATER)) {
                if (dist > 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_WALKED_UNDER_WATER, dist);
                }
            }
            else if (this.isInWater()) {
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_WALKED_ON_WATER, horizontalDist);
                }
            }
            else if (this.onClimbable()) {
                if (dy > 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_CLIMBED, (float) (dy * 1_000));
                }
            }
            else if (this.isOnGround()) {
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    this.isMoving = true;
                    if (this.isSprinting()) {
                        PlayerHelper.addStat(player, EvolutionStats.DISTANCE_SPRINTED, horizontalDist);
                    }
                    else if (this.isCrouching()) {
                        PlayerHelper.addStat(player, EvolutionStats.DISTANCE_CROUCHED, horizontalDist);
                    }
                    else if (this.getPose() == Pose.SWIMMING) {
                        PlayerHelper.addStat(player, EvolutionStats.DISTANCE_PRONE, horizontalDist);
                    }
                    else {
                        PlayerHelper.addStat(player, EvolutionStats.DISTANCE_WALKED, horizontalDist);
                    }
                }
            }
            else if (this.abilities.flying) {
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_FLOWN, horizontalDist);
                }
            }
            else {
                if (dy < 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_FALLEN, (float) (-dy * 1_000));
                }
                else if (dy > 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_JUMPED_VERTICAL, (float) (dy * 1_000));
                }
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_JUMPED_HORIZONTAL, horizontalDist);
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to use Evolution Stats.
     */
    @Overwrite
    private void checkRidingStatistics(double dx, double dy, double dz) {
        //noinspection ConstantConditions
        if ((Object) this instanceof ServerPlayer player && this.isPassenger()) {
            float dist = MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * 1_000;
            if (dist > 0) {
                PlayerHelper.addStat(player, EvolutionStats.TOTAL_DISTANCE_TRAVELED, dist);
            }
        }
    }

    @Override
    public double getBaseAttackDamage() {
        return PlayerHelper.ATTACK_DAMAGE;
    }

    @Override
    public double getBaseHealth() {
        return PlayerHelper.MAX_HEALTH;
    }

    @Override
    public double getBaseMass() {
        return PlayerHelper.MASS;
    }

    @Override
    public double getBaseWalkForce() {
        return PlayerHelper.WALK_FORCE;
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle first person camera.
     */
    @Overwrite
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return PlayerHelper.SIZE_BY_POSE.getOrDefault(pose, PlayerHelper.STANDING_SIZE);
    }

    @Nullable
    @Override
    public HitboxEntity<Player> getHitboxes() {
        return EntityEvents.SKIN_TYPE.getOrDefault(this.getUUID(), SkinType.STEVE) == SkinType.STEVE ?
               EvolutionEntityHitboxes.PLAYER_STEVE :
               EvolutionEntityHitboxes.PLAYER_ALEX;
    }

    @Override
    public double getMotionX() {
        return this.motionX;
    }

    @Override
    public double getMotionY() {
        return this.motionY;
    }

    @Override
    public double getMotionZ() {
        return this.motionZ;
    }

    private float getStepHeightInternal() {
        AttributeInstance massAttribute = this.getAttribute(EvolutionAttributes.MASS.get());
        assert massAttribute != null;
        double baseMass = massAttribute.getBaseValue();
        double totalMass = massAttribute.getValue();
        double equipMass = totalMass - baseMass;
        double stepHeight = 1.062_5f - equipMass * 0.001_14f;
        return (float) Math.max(stepHeight, 0.6);
    }

    @Override
    public double getVolume() {
        return 70_000 * SI.CUBIC_CENTIMETER;
    }

    @Override
    public boolean hasExtendedInventory() {
        return true;
    }

    /**
     * @author TheGreatWolf
     * @reason Remove damage scaling, as difficulty will be handled differently
     */
    @Override
    @Overwrite
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.abilities.invulnerable && !source.isBypassInvul()) {
            return false;
        }
        this.noActionTime = 0;
        if (this.isDeadOrDying()) {
            return false;
        }
        if (!this.level.isClientSide) {
            this.removeEntitiesOnShoulder();
        }
        return amount > 0 && super.hurt(source, amount);
    }

    @Override
    public boolean isCrawling() {
        return this.isCrawling;
    }

    @Override
    public boolean isDiscrete() {
        return this.isShiftKeyDown() || this.getSwimAmount(1.0f) > 0 && this.isOnGround() && !this.isInWater();
    }

    @Override
    public boolean isMoving() {
        return this.isMoving;
    }

    @Inject(method = "startSleepInBed", at = @At("TAIL"))
    private void onStartSleepInBed(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        if (!this.level.isClientSide) {
            //noinspection ConstantConditions
            EvolutionNetwork.send((ServerPlayer) (Object) this, new PacketSCMovement(0, 0, 0));
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (this.isVisuallyCrawling()) {
            this.setSprinting(false);
            this.maxUpStep = this.getStepHeightInternal();
        }
        else {
            this.maxUpStep = 0.6f;
        }
    }

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"))
    private void proxyAiStep(Player player, float amount) {
        //Do nothing
    }

    @Shadow
    protected abstract void removeEntitiesOnShoulder();

    @Override
    public void setCrawling(boolean crawling) {
        this.isCrawling = crawling;
    }

    /**
     * @author TheGreatWolf
     * @reason Replace to handle Evolution's physics
     */
    @Override
    @Overwrite
    public void travel(Vec3 travelVector) {
        if (this.isSwimming() && !this.isPassenger()) {
            double lookVecY = this.getLookAngle().y;
            double d4 = lookVecY < -0.2 ? 0.085 : 0.06;
            if (lookVecY <= 0 ||
                this.jumping ||
                !this.level.getBlockState(new BlockPos(this.getX(), this.getY() + 0.9, this.getZ())).getFluidState().isEmpty()) {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.add(0, (lookVecY - motion.y) * d4, 0));
            }
        }
        if (this.abilities.flying && !this.isPassenger()) {
            double motionY = this.getDeltaMovement().y;
            float jumpMovementFactor = this.flyingSpeed;
            this.flyingSpeed = 4 * this.abilities.getFlyingSpeed() * (this.isSprinting() ? 2 : 1);
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, motionY * 0.8, motion.z);
            this.fallDistance = 0.0F;
            super.travel(travelVector);
            this.setSharedFlag(FLAG_FALL_FLYING, false);
            this.flyingSpeed = jumpMovementFactor;
        }
        else {
            super.travel(travelVector);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Handle crawling pose
     */
    @Overwrite
    protected void updatePlayerPose() {
        if (this.forcedPose != null) {
            this.setPose(this.forcedPose);
            return;
        }
        if (this.isPassenger()) {
            this.setPose(Pose.STANDING);
        }
        else if (this.canEnterPose(Pose.SWIMMING)) {
            Pose firstPose;
            if (this.isFallFlying()) {
                firstPose = Pose.FALL_FLYING;
            }
            else if (this.isSleeping()) {
                firstPose = Pose.SLEEPING;
            }
            else if (this.isSwimming() || this.isCrawling) {
                firstPose = Pose.SWIMMING;
            }
            else if (this.isAutoSpinAttack()) {
                firstPose = Pose.SPIN_ATTACK;
            }
            else if (this.isShiftKeyDown() && !this.abilities.flying) {
                firstPose = Pose.CROUCHING;
            }
            else {
                firstPose = Pose.STANDING;
            }
            Pose secondPose;
            if (!this.isSpectator() && !this.isPassenger() && !this.canEnterPose(firstPose)) {
                if (this.canEnterPose(Pose.CROUCHING)) {
                    secondPose = Pose.CROUCHING;
                }
                else {
                    secondPose = Pose.SWIMMING;
                }
            }
            else {
                secondPose = firstPose;
            }
            this.setPose(secondPose);
        }
    }
}
