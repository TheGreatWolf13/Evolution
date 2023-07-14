package tgw.evolution.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.world.util.CollisionShapeCalculator;

@Mixin(CollisionGetter.class)
public interface MixinCollisionGetter {

    @Overwrite
    private @Nullable VoxelShape borderCollision(Entity entity, AABB bb) {
        WorldBorder worldBorder = this.getWorldBorder();
        return worldBorder.isInsideCloseToBorder_(entity, bb.getXsize(), bb.getZsize()) ? worldBorder.getCollisionShape() : null;
    }

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

    @Shadow
    WorldBorder getWorldBorder();
}
