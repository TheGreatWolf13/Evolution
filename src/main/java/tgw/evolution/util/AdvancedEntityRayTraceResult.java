package tgw.evolution.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.util.hitbox.Hitbox;

import javax.annotation.Nullable;

public class AdvancedEntityRayTraceResult extends EntityHitResult {

    @Nullable
    private final Hitbox hitbox;

    public AdvancedEntityRayTraceResult(Entity entity, Vec3 hitVec, @Nullable Hitbox hitbox) {
        super(entity, hitVec);
        this.hitbox = hitbox;
    }

    @Nullable
    public Hitbox getHitbox() {
        return this.hitbox;
    }
}
