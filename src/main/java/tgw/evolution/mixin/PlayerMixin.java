package tgw.evolution.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.network.PacketSCMovement;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.patches.IPlayerPatch;
import tgw.evolution.util.PlayerHelper;
import tgw.evolution.util.constants.EntityFlags;
import tgw.evolution.util.constants.SkinType;
import tgw.evolution.util.hitbox.EvolutionEntityHitboxes;
import tgw.evolution.util.hitbox.HitboxEntity;
import tgw.evolution.util.math.MathHelper;

import javax.annotation.Nullable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements INeckPosition, IEntityPatch, IPlayerPatch {

    @Shadow
    @Final
    private Abilities abilities;
    private boolean isMoving;

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to use Evolution Stats.
     */
    @Overwrite
    public void checkMovementStatistics(double dx, double dy, double dz) {
        this.isMoving = false;
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
     * @author MGSchultz
     * <p>
     * Overwrite to use Evolution Stats.
     */
    @Overwrite
    private void checkRidingStatistics(double dx, double dy, double dz) {
        if ((Object) this instanceof ServerPlayer player && this.isPassenger()) {
            float dist = MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * 1_000;
            if (dist > 0) {
                PlayerHelper.addStat(player, EvolutionStats.TOTAL_DISTANCE_TRAVELED, dist);
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
    public EntityDimensions getDimensions(Pose pose) {
        return PlayerHelper.SIZE_BY_POSE.getOrDefault(pose, PlayerHelper.STANDING_SIZE);
    }

    @Nullable
    @Override
    public HitboxEntity<? extends Entity> getHitboxes() {
        return EntityEvents.SKIN_TYPE.getOrDefault(this.getUUID(), SkinType.STEVE) == SkinType.STEVE ?
               EvolutionEntityHitboxes.PLAYER_STEVE :
               EvolutionEntityHitboxes.PLAYER_ALEX;
    }

    @Override
    public Vec3 getNeckPoint() {
        switch (this.getPose()) {
            case CROUCHING -> {
                return PlayerHelper.NECK_POS_SNEAKING;
            }
            case SWIMMING -> {
                if (!this.isInWater()) {
                    return PlayerHelper.NECK_POS_CRAWLING;
                }
                return PlayerHelper.getSwimmingNeckPoint(this.getXRot());
            }
        }
        float swimAnimation = MathHelper.getSwimAnimation(this, 1.0f);
        if (swimAnimation > 0) {
            return PlayerHelper.NECK_POS_CRAWLING;
        }
        return PlayerHelper.NECK_POS_STANDING;
    }

    @Override
    public boolean hasHitboxes() {
        return true;
    }

    @Override
    public boolean isMoving() {
        return this.isMoving;
    }

    @Inject(method = "startSleepInBed", at = @At("TAIL"))
    private void onStartSleepInBed(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        if (!this.level.isClientSide) {
            EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) (Object) this), new PacketSCMovement(0, 0, 0));
        }
    }

    /**
     * @author MGSchultz
     * <p>
     * Replace to handle Evolution's physics
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
            this.setSharedFlag(EntityFlags.ELYTRA_FLYING, false);
            this.flyingSpeed = jumpMovementFactor;
        }
        else {
            super.travel(travelVector);
        }
    }
}
