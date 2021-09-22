package tgw.evolution.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.entities.IEntityPatch;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.network.PacketSCMovement;
import tgw.evolution.util.EntityFlags;
import tgw.evolution.util.PlayerHelper;
import tgw.evolution.util.SkinType;
import tgw.evolution.util.hitbox.EvolutionEntityHitboxes;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements INeckPosition, IEntityPatch {

    @Shadow
    @Final
    public PlayerAbilities abilities;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to use Evolution Stats.
     */
    @Overwrite
    public void checkMovementStatistics(double dx, double dy, double dz) {
        if (!this.isPassenger() && (Object) this instanceof ServerPlayerEntity) {
            float dist = MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * 1_000;
            if (dist > 0) {
                PlayerHelper.addStat((PlayerEntity) (Object) this, EvolutionStats.TOTAL_DISTANCE_TRAVELED, dist);
            }
            if (this.isSwimming()) {
                if (dist > 0) {
                    PlayerHelper.addStat((PlayerEntity) (Object) this, EvolutionStats.DISTANCE_SWUM, dist);
                }
            }
            else if (this.isEyeInFluid(FluidTags.WATER)) {
                if (dist > 0) {
                    PlayerHelper.addStat((PlayerEntity) (Object) this, EvolutionStats.DISTANCE_WALKED_UNDER_WATER, dist);
                }
            }
            else if (this.isInWater()) {
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    PlayerHelper.addStat((PlayerEntity) (Object) this, EvolutionStats.DISTANCE_WALKED_ON_WATER, horizontalDist);
                }
            }
            else if (this.onClimbable()) {
                if (dy > 0) {
                    PlayerHelper.addStat((PlayerEntity) (Object) this, EvolutionStats.DISTANCE_CLIMBED, (float) (dy * 1_000));
                }
            }
            else if (this.isOnGround()) {
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    if (this.isSprinting()) {
                        PlayerHelper.addStat((PlayerEntity) (Object) this, EvolutionStats.DISTANCE_SPRINTED, horizontalDist);
                    }
                    else if (this.isCrouching()) {
                        PlayerHelper.addStat((PlayerEntity) (Object) this, EvolutionStats.DISTANCE_CROUCHED, horizontalDist);
                    }
                    else if (this.getPose() == Pose.SWIMMING) {
                        PlayerHelper.addStat((PlayerEntity) (Object) this, EvolutionStats.DISTANCE_PRONE, horizontalDist);
                    }
                    else {
                        PlayerHelper.addStat((PlayerEntity) (Object) this, EvolutionStats.DISTANCE_WALKED, horizontalDist);
                    }
                }
            }
            else if (this.abilities.flying) {
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    PlayerHelper.addStat((PlayerEntity) (Object) this, EvolutionStats.DISTANCE_FLOWN, horizontalDist);
                }
            }
            else {
                if (dy < 0) {
                    PlayerHelper.addStat((PlayerEntity) (Object) this, EvolutionStats.DISTANCE_FALLEN, (float) (-dy * 1_000));
                }
                else if (dy > 0) {
                    PlayerHelper.addStat((PlayerEntity) (Object) this, EvolutionStats.DISTANCE_JUMPED_VERTICAL, (float) (dy * 1_000));
                }
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    PlayerHelper.addStat((PlayerEntity) (Object) this, EvolutionStats.DISTANCE_JUMPED_HORIZONTAL, horizontalDist);
                }
            }
        }
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to use Evolution Stats.
     */
    @Overwrite
    private void checkRidingStatistics(double dx, double dy, double dz) {
        if ((Object) this instanceof ServerPlayerEntity && this.isPassenger()) {
            float dist = MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * 1_000;
            if (dist > 0) {
                PlayerHelper.addStat((PlayerEntity) (Object) this, EvolutionStats.TOTAL_DISTANCE_TRAVELED, dist);
            }
        }
    }

    @Override
    public float getCameraYOffset() {
        return 4 / 16.0f * 0.937_5f;
    }

    @Override
    public float getCameraZOffset() {
        return 4 / 16.0f * 0.937_5f;
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to handle first person camera.
     */
    @Overwrite
    @Override
    public EntitySize getDimensions(Pose pose) {
        return PlayerHelper.SIZE_BY_POSE.getOrDefault(pose, PlayerHelper.STANDING_SIZE);
    }

    @Nullable
    @Override
    public HitboxEntity<? extends Entity> getHitboxes() {
        return EntityEvents.SKIN_TYPE.getOrDefault(this.getUUID(), SkinType.STEVE) == SkinType.STEVE ?
               EvolutionEntityHitboxes.PLAYER_STEVE :
               EvolutionEntityHitboxes.PLAYER_ALEX;
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to use Evolution Damage Sources.
     */
    @Nullable
    @Override
    @Overwrite
    protected SoundEvent getHurtSound(DamageSource source) {
        if (source == EvolutionDamage.ON_FIRE) {
            return SoundEvents.PLAYER_HURT_ON_FIRE;
        }
        if (source == EvolutionDamage.DROWN) {
            return SoundEvents.PLAYER_HURT_DROWN;
        }
        return source == DamageSource.SWEET_BERRY_BUSH ? SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH : SoundEvents.PLAYER_HURT;
    }

    @Override
    public Vector3d getNeckPoint() {
        switch (this.getPose()) {
            case CROUCHING: {
                return PlayerHelper.NECK_POS_SNEAKING;
            }
        }
        return PlayerHelper.NECK_POS_STANDING;
    }

    @Override
    public boolean hasHitboxes() {
        return true;
    }

    @Inject(method = "startSleepInBed", at = @At("TAIL"))
    private void onStartSleepInBed(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepResult, Unit>> cir) {
        if (!this.level.isClientSide) {
            EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) (Object) this), new PacketSCMovement(0, 0, 0));
        }
    }

    /**
     * @author MGSchultz
     * <p>
     * Replace to handle Evolution's physics
     */
    @Override
    @Overwrite
    public void travel(Vector3d travelVector) {
        if (this.isSwimming() && !this.isPassenger()) {
            double lookVecY = this.getLookAngle().y;
            double d4 = lookVecY < -0.2 ? 0.085 : 0.06;
            if (lookVecY <= 0 ||
                this.jumping ||
                !this.level.getBlockState(new BlockPos(this.getX(), this.getY() + 0.9, this.getZ())).getFluidState().isEmpty()) {
                Vector3d motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.add(0, (lookVecY - motion.y) * d4, 0));
            }
        }
        if (this.abilities.flying && !this.isPassenger()) {
            double motionY = this.getDeltaMovement().y;
            float jumpMovementFactor = this.flyingSpeed;
            this.flyingSpeed = 4 * this.abilities.getFlyingSpeed() * (this.isSprinting() ? 2 : 1);
            Vector3d motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, motionY * 0.8, motion.z);
            this.fallDistance = 0.0F;
            super.travel(travelVector);
            this.setSharedFlag(EntityFlags.ELYTRA_FLYING, false);
            this.flyingSpeed = jumpMovementFactor;
        }
        else {
            super.travel(travelVector);
        }
    }
}
