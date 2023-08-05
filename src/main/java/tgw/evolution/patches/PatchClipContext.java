package tgw.evolution.patches;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface PatchClipContext {

    default VoxelShape getBlockShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default VoxelShape getFluidShape_(FluidState state, BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void setBlock(ClipContext.Block block) {
        throw new AbstractMethodError();
    }

    default void setEntity(@Nullable Entity entity) {
        throw new AbstractMethodError();
    }

    default void setFluid(ClipContext.Fluid fluid) {
        throw new AbstractMethodError();
    }
}
