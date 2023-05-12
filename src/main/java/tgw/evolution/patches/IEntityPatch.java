package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
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

    default int getFireDamageImmunity() {
        return 0;
    }

    float getFrictionModifier();

    @Nullable
    HitboxEntity<T> getHitboxes();

    /**
     * @return Controls the deceleration because of the motion of your legs.
     */
    double getLegSlowdown();

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

    default boolean hasCollidedOnXAxis() {
        return false;
    }

    default boolean hasCollidedOnZAxis() {
        return false;
    }

    default boolean hasExtendedInventory() {
        return false;
    }

    default void setFireDamageImmunity(int immunity) {
    }
}
