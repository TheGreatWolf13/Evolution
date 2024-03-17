package tgw.evolution.blocks;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IStable extends IStructural {

    @Override
    default boolean canMakeABeamWith(BlockState thisState, BlockState otherState) {
        return false;
    }

    @Override
    default void fail(Level level, BlockState state, int x, int y, int z) {
        //Do nothing, a stable block cannot fail :)
    }

    @Override
    default BeamType getBeamType(BlockState state) {
        return BeamType.NONE;
    }

    @Override
    default int getIntegrity(BlockState state) {
        return MAX_INTEGRITY;
    }

    @Override
    default Stabilization getStabilization(BlockState state) {
        return Stabilization.NONE;
    }
}
