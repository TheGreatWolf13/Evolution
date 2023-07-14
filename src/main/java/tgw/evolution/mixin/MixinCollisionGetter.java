package tgw.evolution.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.world.util.CollisionShapeCalculator;

import javax.annotation.Nullable;

@Mixin(CollisionGetter.class)
public interface MixinCollisionGetter {

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations.
     */
    @Overwrite
    default boolean collidesWithSuffocatingBlock(@Nullable Entity entity, AABB box) {
        try (CollisionShapeCalculator calculator = CollisionShapeCalculator.getInstance((CollisionGetter) this, entity, box, true)) {
            for (VoxelShape shape : calculator) {
                if (!shape.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }
}
