package tgw.evolution.util.hitbox.hrs;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface HREntity<T extends Entity> {

    default Vec3 renderOffset(T entity, float partialTicks) {
        return Vec3.ZERO;
    }

    void setShadowRadius(float radius);
}
