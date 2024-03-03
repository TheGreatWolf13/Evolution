package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;

@Mixin(EntityCollisionContext.class)
public abstract class MixinEntityCollisionContext implements CollisionContext {

    @Shadow @Final private double entityBottom;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public boolean isAbove(VoxelShape shape, BlockPos pos, boolean canAscend) {
        Evolution.deprecatedMethod();
        return this.isAbove_(shape, pos.getX(), pos.getY(), pos.getZ(), canAscend);
    }

    @Override
    public boolean isAbove_(VoxelShape shape, int x, int y, int z, boolean canAscend) {
        return this.entityBottom > y + shape.max(Direction.Axis.Y) - 1e-5;
    }
}
