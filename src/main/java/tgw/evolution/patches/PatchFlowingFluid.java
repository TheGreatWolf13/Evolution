package tgw.evolution.patches;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface PatchFlowingFluid {

    default void beforeDestroyingBlock_(LevelAccessor level, int x, int y, int z, BlockState state) {
        throw new AbstractMethodError();
    }

    default int getSpreadDelay(Level level, int x, int y, int z, FluidState state, FluidState newState) {
        throw new AbstractMethodError();
    }

    default void spreadTo_(LevelAccessor level, int x, int y, int z, BlockState state, Direction direction, FluidState fluid) {
        throw new AbstractMethodError();
    }
}
