package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.physics.SI;

public interface IEntityPatch<T extends Entity> {

    default double getAcceleration() {
        return 0;
    }

    default double getBaseAttackDamage() {
        return 1;
    }

    default double getBaseHealth() {
        return 25;
    }

    /**
     * @return The entity mass in kg.
     */
    default double getBaseMass() {
        return 1;
    }

    default double getBaseWalkForce() {
        return 1 * SI.NEWTON;
    }

    float getFrictionModifier();

    default BlockPos getFrictionPos() {
        return BlockPos.ZERO;
    }

    @Nullable
    HitboxEntity<T> getHitboxes();

    default @Nullable Pose getLastPose() {
        return null;
    }

    /**
     * @return Controls the deceleration because of the motion of your legs.
     */
    double getLegSlowdown();

    default double getLungCapacity() {
        return 0;
    }

    default int getNoJumpDelay() {
        return 0;
    }

    default BlockPos getSteppingPos() {
        return BlockPos.ZERO;
    }

    double getVolume();

    default double getVolumeCorrectionFactor() {
        return 0;
    }

    default boolean hasAnyFluidInEye() {
        return false;
    }

    default boolean hasCollidedOnXAxis() {
        return false;
    }

    default boolean hasCollidedOnZAxis() {
        return false;
    }

    default boolean hasExtendedInventory() {
        return false;
    }

    default boolean isFullySubmerged(TagKey<Fluid> fluid) {
        return false;
    }

    default boolean isInAnyFluid() {
        return false;
    }
}
