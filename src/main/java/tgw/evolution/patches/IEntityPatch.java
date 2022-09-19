package tgw.evolution.patches;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.util.hitbox.HitboxEntity;
import tgw.evolution.util.hitbox.HitboxType;

public interface IEntityPatch<T extends Entity> {

    /**
     * @return The entity mass in kg.
     */
    double getBaseMass();

    default float getDamageForHitbox(float amount, EvolutionDamage.Type type, HitboxType hitbox) {
        return amount * hitbox.getMultiplier();
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

    default boolean hurtInternal(DamageSource source, float damage) {
        return false;
    }

    default boolean hurtSpecial(DamageSource source, EvolutionDamage.Type type, float amount, HitboxType hitbox) {
        return this.hurtInternal(source, this.getDamageForHitbox(amount, type, hitbox));
    }

    default void setFireDamageImmunity(int immunity) {
    }
}
