package tgw.evolution.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import tgw.evolution.util.hitbox.Hitbox;

import javax.annotation.Nullable;

public class AdvancedEntityRayTraceResult extends EntityRayTraceResult {

    @Nullable
    private final Hitbox hitbox;

    public AdvancedEntityRayTraceResult(Entity entity, Vector3d hitVec, @Nullable Hitbox hitbox) {
        super(entity, hitVec);
        this.hitbox = hitbox;
    }

    @Nullable
    public Hitbox getHitbox() {
        return this.hitbox;
    }
}
