package tgw.evolution.patches;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.math.Units;

public interface IEntityPatch<T extends Entity> {

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
        return 1 * Units.NEWTON;
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
