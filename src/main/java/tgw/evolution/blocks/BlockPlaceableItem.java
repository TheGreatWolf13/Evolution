package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.tileentities.TELoggable;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;

import static tgw.evolution.init.EvolutionBStates.FLUID_LOGGED;

public class BlockPlaceableItem extends BlockMass implements IReplaceable, IFluidLoggable {

    public BlockPlaceableItem(Properties properties) {
        super(properties, 0);
        this.registerDefaultState(this.defaultBlockState().setValue(FLUID_LOGGED, false));
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return true;
    }

    @Override
    public boolean canFlowThrough(BlockState state, Direction direction) {
        return true;
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        return BlockUtils.hasSolidSide(world, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FLUID_LOGGED);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TELoggable();
    }

    @Override
    public NonNullList<ItemStack> getDrops(World world, BlockPos pos, BlockState state) {
        return NonNullList.of(ItemStack.EMPTY, new ItemStack(this));
    }

    @Override
    public int getFluidCapacity(BlockState state) {
        return 100_000;
    }

    @Override
    public int getInitialAmount(BlockState state) {
        return 0;
    }

    @Override
    public int getMass(World world, BlockPos pos, BlockState state) {
        int mass = 0;
        if (state.getValue(FLUID_LOGGED)) {
            Fluid fluid = this.getFluid(world, pos);
            if (fluid instanceof FluidGeneric) {
                int amount = this.getCurrentAmount(world, pos, state);
                int layers = MathHelper.ceil(amount / 12_500.0);
                mass = layers * ((FluidGeneric) fluid).getMass() / 8;
            }
        }
        return mass + this.getBaseMass();
    }

    @Override
    public OffsetType getOffsetType() {
        return OffsetType.XZ;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        Vector3d vec3d = state.getOffset(world, pos);
        return EvolutionHitBoxes.GROUND_ITEM.move(vec3d.x, vec3d.y, vec3d.z);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getValue(FLUID_LOGGED);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            if (!state.canSurvive(world, pos)) {
                dropResources(state, world, pos);
                world.removeBlock(pos, false);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(FLUID_LOGGED)) {
            BlockUtils.scheduleFluidTick(world, currentPos);
        }
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }
}
