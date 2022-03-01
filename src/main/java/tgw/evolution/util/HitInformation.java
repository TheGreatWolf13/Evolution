package tgw.evolution.util;

import net.minecraft.world.entity.Entity;
import tgw.evolution.util.hitbox.HitboxType;

import java.util.EnumSet;
import java.util.Set;

public class HitInformation {

    private final Entity entity;
    private final Set<HitboxType> hitboxes = EnumSet.noneOf(HitboxType.class);

    public HitInformation(Entity entity) {
        this.entity = entity;
    }

    public void addHitbox(HitboxType hitbox) {
        this.hitboxes.add(hitbox);
    }

    public Entity getEntity() {
        return this.entity;
    }

    public Set<HitboxType> getHitboxes() {
        return this.hitboxes;
    }
}
