package tgw.evolution.entities;

import net.minecraft.entity.Entity;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;

public interface IEntityPatch {

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

    void setFireDamageImmunity(int immunity);
}
