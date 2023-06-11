package tgw.evolution.patches;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FluidState;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.physics.Fluid;

public interface IFluidPatch {

    default Fluid fluid() {
        return Fluid.VACUUM;
    }

    /**
     * Computes the flow for the FluidState with a mutable Vec3d.
     * The Vec3d should be zeroed when passed in.
     */
    @CanIgnoreReturnValue
    default Vec3d getFlow(BlockGetter level, BlockPos pos, FluidState fluidState, BlockPos.MutableBlockPos mutablePos, Vec3d flow) {
        return flow;
    }

    double getFlowStrength(DimensionType type);
}
