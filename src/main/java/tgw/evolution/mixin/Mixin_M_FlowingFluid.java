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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.math.VectorUtil;

import java.util.Map;

@Mixin(FlowingFluid.class)
public abstract class Mixin_M_FlowingFluid extends Fluid {

    @Shadow @Final public static BooleanProperty FALLING;
    @Shadow @Final private Map<FluidState, VoxelShape> shapes;

    @Unique
    private static boolean hasSameAbove_(FluidState fluidState, BlockGetter level, int x, int y, int z) {
        return fluidState.getType().isSame(level.getFluidState_(x, y + 1, z).getType());
    }

    @Shadow
    protected abstract boolean affectsFlow(FluidState pState);

    @Override
    public Vec3d getFlow(BlockGetter level, int x, int y, int z, FluidState state, Vec3d flow) {
        double flowX = 0;
        double flowZ = 0;
        for (Direction dir : DirectionUtil.HORIZ_NESW) {
            int offX = x + dir.getStepX();
            int offZ = z + dir.getStepZ();
            FluidState fluidAtSide = level.getFluidState_(offX, y, offZ);
            if (this.affectsFlow(fluidAtSide)) {
                float ownHeightAtSide = fluidAtSide.getOwnHeight();
                float dHeight = 0.0F;
                if (ownHeightAtSide == 0.0F) {
                    if (!level.getBlockState_(offX, y, offZ).getMaterial().blocksMotion()) {
                        FluidState fluidAtSideBelow = level.getFluidState_(offX, y - 1, offZ);
                        if (this.affectsFlow(fluidAtSideBelow)) {
                            ownHeightAtSide = fluidAtSideBelow.getOwnHeight();
                            if (ownHeightAtSide > 0.0F) {
                                dHeight = state.getOwnHeight() - (ownHeightAtSide - 0.888_888_9F);
                            }
                        }
                    }
                }
                else if (ownHeightAtSide > 0.0F) {
                    dHeight = state.getOwnHeight() - ownHeightAtSide;
                }
                if (dHeight != 0.0F) {
                    flowX += dir.getStepX() * dHeight;
                    flowZ += dir.getStepZ() * dHeight;
                }
            }
        }
        double flowY = 0;
        if (state.getValue(FALLING)) {
            for (Direction dir : DirectionUtil.HORIZ_NESW) {
                int offX = x + dir.getStepX();
                int offZ = z + dir.getStepZ();
                if (this.isSolidFace(level, offX, y, offZ, dir) || this.isSolidFace(level, offX, y + 1, offZ, dir)) {
                    double norm = VectorUtil.norm(flowX, flowY, flowZ);
                    flowX *= norm;
                    flowY *= norm;
                    flowZ *= norm;
                    flowY -= 6;
                    break;
                }
            }
        }
        double norm = VectorUtil.norm(flowX, flowY, flowZ);
        return flow.set(flowX * norm, flowY * norm, flowZ * norm);
    }

    @Override
    @Overwrite
    public Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState fluidState) {
        Evolution.deprecatedMethod();
        return this.getFlow(level, pos.getX(), pos.getY(), pos.getZ(), fluidState, new Vec3d());
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
    @DeleteMethod
    public boolean isSolidFace(BlockGetter level, BlockPos pos, Direction dir) {
        throw new AbstractMethodError();
    }

    @Unique
    private boolean isSolidFace(BlockGetter level, int x, int y, int z, Direction dir) {
        if (level.getFluidState_(x, y, z).getType().isSame(this)) {
            return false;
        }
        if (dir == Direction.UP) {
            return true;
        }
        BlockState blockState = level.getBlockState_(x, y, z);
        return blockState.getMaterial() != Material.ICE && blockState.isFaceSturdy_(level, x, y, z, dir);
    }
}
