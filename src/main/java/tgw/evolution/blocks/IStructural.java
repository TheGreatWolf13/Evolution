package tgw.evolution.blocks;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.blocks.util.BlockUtils;

public interface IStructural extends IFillable {

    int MAX_INTEGRITY = 255;

    boolean canMakeABeamWith(BlockState thisState, BlockState otherState);

    default void fail(Level level, BlockState state, int x, int y, int z) {
        if (this instanceof IFallable fallable && !(level.getBlockState_(x, y - 1, z).getBlock() instanceof IFillable)) {
            fallable.fall(level, x, y, z);
            return;
        }
        level.removeBlock_(x, y, z, false);
        BlockUtils.dropResources(state, level, x, y, z);
    }

    BeamType getBeamType(BlockState state);

    default int getIncrementForBeam(BlockState state) {
        return 1;
    }

    int getIntegrity(BlockState state);

    Stabilization getStabilization(BlockState state);

    enum BeamType {
        NONE,
        CARDINAL_ARCH,
        CARDINAL_BEAM,
        X_BEAM,
        Z_BEAM,
        X_ARCH,
        Z_ARCH;

        public static final BeamType[] VALUES = values();
    }

    enum Stabilization {
        NONE,
        ARCH,
        BEAM
    }
}
