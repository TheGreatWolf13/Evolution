package tgw.evolution.patches;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.physics.Fluid;

import java.util.Random;

public interface PatchFluid {

    default Fluid fluid() {
        return Fluid.VACUUM;
    }

    /**
     * Computes the flow for the FluidState with a mutable Vec3d.
     * The Vec3d should be zeroed when passed in.
     */
    @CanIgnoreReturnValue
    default Vec3d getFlow(BlockGetter level, int x, int y, int z, FluidState fluidState, BlockPos.MutableBlockPos mutablePos, Vec3d flow) {
        return flow;
    }

    default double getFlowStrength(DimensionType type) {
        throw new AbstractMethodError();
    }

    default float getHeight_(FluidState fluidState, BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default VoxelShape getShape_(FluidState fluidState, BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void randomTick_(Level level, int x, int y, int z, FluidState fluidState, Random random) {
        throw new AbstractMethodError();
    }
}
