package tgw.evolution.entities;

import net.minecraft.world.entity.Entity;
import tgw.evolution.util.hitbox.HitboxEntity;

import javax.annotation.Nullable;

public interface IEvolutionEntity<T extends Entity> {

    @Nullable
    HitboxEntity<T> getHitbox();

    boolean hasHitboxes();
}
