package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchCollisionGetter;
import tgw.evolution.world.util.CollisionShapeCalculator;

@Mixin(CollisionGetter.class)
public interface MixinCollisionGetter extends PatchCollisionGetter, BlockGetter {

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

    @Shadow
    boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape);

    @Overwrite
    default boolean isUnobstructed(BlockState state, BlockPos pos, CollisionContext context) {
        Evolution.deprecatedMethod();
        return this.isUnobstructed_(state, pos.getX(), pos.getY(), pos.getZ(), context instanceof EntityCollisionContext c ? c.getEntity() : null);
    }

    @Override
    default boolean isUnobstructed_(BlockState state,
                                    int x,
                                    int y,
                                    int z,
                                    @Nullable Entity entity) {
        VoxelShape shape = state.getCollisionShape_(this, x, y, z, entity);
        return shape.isEmpty() || this.isUnobstructed(null, shape.move(x, y, z));
    }
}
