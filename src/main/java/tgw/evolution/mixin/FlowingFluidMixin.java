package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IFluidPatch;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.Vec3d;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin extends Fluid implements IFluidPatch {

    @Shadow
    @Final
    public static BooleanProperty FALLING;

    @Shadow
    protected abstract boolean affectsFlow(FluidState pState);

    @Override
    public Vec3d getFlow(BlockGetter level, BlockPos pos, FluidState state, BlockPos.MutableBlockPos mutablePos, Vec3d flow) {
        double dx = 0;
        double dz = 0;
        for (Direction direction : DirectionUtil.HORIZ_NESW) {
            mutablePos.setWithOffset(pos, direction);
            FluidState fluidState = level.getFluidState(mutablePos);
            if (this.affectsFlow(fluidState)) {
                float height = fluidState.getOwnHeight();
                float dHeight = 0.0F;
                if (height == 0.0F) {
                    if (!level.getBlockState(mutablePos).getMaterial().blocksMotion()) {
                        FluidState stateBelow = level.getFluidState(mutablePos.move(Direction.DOWN));
                        if (this.affectsFlow(stateBelow)) {
                            height = stateBelow.getOwnHeight();
                            if (height > 0) {
                                dHeight = state.getOwnHeight() - (height - 8 / 9.0f);
                            }
                        }
                    }
                }
                else if (height > 0.0F) {
                    dHeight = state.getOwnHeight() - height;
                }
                if (dHeight != 0.0F) {
                    dx += direction.getStepX() * dHeight;
                    dz += direction.getStepZ() * dHeight;
                }
            }
        }
        flow.set(dx, 0, dz);
        if (state.getValue(FALLING)) {
            for (Direction direction : DirectionUtil.HORIZ_NESW) {
                mutablePos.setWithOffset(pos, direction);
                if (this.isSolidFace(level, mutablePos, direction) || this.isSolidFace(level, mutablePos.move(Direction.UP), direction)) {
                    flow.normalizeMutable().addMutable(0, -6, 0);
                    break;
                }
            }
        }
        return flow.normalizeMutable();
    }

    @Shadow
    protected abstract boolean isSolidFace(BlockGetter pLevel, BlockPos pNeighborPos, Direction pSide);
}
