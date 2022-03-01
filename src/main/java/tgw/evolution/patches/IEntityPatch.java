package tgw.evolution.patches;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.util.hitbox.HitboxEntity;
import tgw.evolution.util.hitbox.HitboxType;

import javax.annotation.Nullable;

public interface IEntityPatch {

    default float getDamageForHitbox(float amount, EvolutionDamage.Type type, HitboxType hitbox) {
        return amount * hitbox.getMultiplier();
    }

    int getFireDamageImmunity();

    @Nullable
    default HitboxEntity<? extends Entity> getHitboxes() {
        return null;
    }

    boolean hasCollidedOnXAxis();

    boolean hasCollidedOnZAxis();

    default boolean hasHitboxes() {
        return false;
    }

    boolean hurtInternal(DamageSource source, float damage);

    default boolean hurtSpecial(DamageSource source, EvolutionDamage.Type type, float amount, HitboxType hitbox) {
        return this.hurtInternal(source, this.getDamageForHitbox(amount, type, hitbox));
    }

    void setFireDamageImmunity(int immunity);
}
