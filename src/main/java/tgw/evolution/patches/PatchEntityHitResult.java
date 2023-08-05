package tgw.evolution.patches;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

public interface PatchEntityHitResult {

    default EntityHitResult create(Entity entity, double x, double y, double z) {
        EntityHitResult hitResult = new EntityHitResult(entity);
        hitResult.set(x, y, z);
        return hitResult;
    }
}
