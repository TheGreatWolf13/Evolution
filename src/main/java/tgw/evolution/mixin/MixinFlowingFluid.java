package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.Vec3d;

import java.util.Map;

@Mixin(FlowingFluid.class)
public abstract class MixinFlowingFluid extends Fluid {

    @Shadow @Final public static BooleanProperty FALLING;
    @Shadow @Final private Map<FluidState, VoxelShape> shapes;

    @Unique
    private static boolean hasSameAbove_(FluidState fluidState, BlockGetter level, int x, int y, int z) {
        return fluidState.getType().isSame(level.getFluidState_(x, y + 1, z).getType());
    }

    @Shadow
    protected abstract boolean affectsFlow(FluidState pState);

    @Override
    public Vec3d getFlow(BlockGetter level, int x, int y, int z, FluidState state, BlockPos.MutableBlockPos mutablePos, Vec3d flow) {
        double dx = 0;
        double dz = 0;
        for (Direction direction : DirectionUtil.HORIZ_NESW) {
            FluidState fluidState = level.getFluidStateAtSide(x, y, z, direction);
            if (this.affectsFlow(fluidState)) {
                float height = fluidState.getOwnHeight();
                float dHeight = 0.0F;
                if (height == 0.0F) {
                    if (!level.getBlockStateAtSide(x, y, z, direction).getMaterial().blocksMotion()) {
                        FluidState stateBelow = level.getFluidStateAtSide(x, y - 1, z, direction);
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
                mutablePos.set(x, y, z).move(direction);
                if (this.isSolidFace(level, mutablePos, direction) || this.isSolidFace(level, mutablePos.move(Direction.UP), direction)) {
                    flow.normalizeMutable().addMutable(0, -6, 0);
                    break;
                }
            }
        }
        return flow.normalizeMutable();
    }

    @Override
    @Overwrite
    public float getHeight(FluidState fluidState, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getHeight_(fluidState, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public float getHeight_(FluidState fluidState, BlockGetter level, int x, int y, int z) {
        return hasSameAbove_(fluidState, level, x, y, z) ? 1.0F : fluidState.getOwnHeight();
    }

    @Override
    @Overwrite
    public VoxelShape getShape(FluidState fluidState, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getShape_(fluidState, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public VoxelShape getShape_(FluidState fluidState, BlockGetter level, int x, int y, int z) {
        if (fluidState.getAmount() == 9 && hasSameAbove_(fluidState, level, x, y, z)) {
            return Shapes.block();
        }
        VoxelShape shape = this.shapes.get(fluidState);
        if (shape == null) {
            shape = Shapes.box(0, 0, 0, 1, fluidState.getHeight_(level, x, y, z), 1);
            this.shapes.put(fluidState, shape);
        }
        return shape;
    }

    @Overwrite
    public boolean isSolidFace(BlockGetter level, BlockPos pos, Direction dir) {
        if (level.getFluidState_(pos).getType().isSame(this)) {
            return false;
        }
        if (dir == Direction.UP) {
            return true;
        }
        BlockState blockState = level.getBlockState_(pos);
        return blockState.getMaterial() != Material.ICE && blockState.isFaceSturdy(level, pos, dir);
    }
}
