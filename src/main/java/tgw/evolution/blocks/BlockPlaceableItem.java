package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.tileentities.TELoggable;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;

import static tgw.evolution.init.EvolutionBStates.FLUIDLOGGED;

public class BlockPlaceableItem extends BlockMass implements IReplaceable, IFluidLoggable {

    public BlockPlaceableItem(Properties properties) {
        super(properties, 0);
        this.setDefaultState(this.getDefaultState().with(FLUIDLOGGED, false));
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

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TELoggable();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FLUIDLOGGED);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return new ItemStack(this);
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
        if (state.get(FLUIDLOGGED)) {
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
        Vec3d vec3d = state.getOffset(world, pos);
        return EvolutionHitBoxes.GROUND_ITEM.withOffset(vec3d.x, vec3d.y, vec3d.z);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.get(FLUIDLOGGED);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public boolean isSolid(BlockState state) {
        return false;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        BlockPos posDown = pos.down();
        BlockState down = world.getBlockState(posDown);
        return Block.hasSolidSide(down, world, posDown, Direction.UP);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isRemote) {
            if (!state.isValidPosition(world, pos)) {
                spawnDrops(state, world, pos);
                world.removeBlock(pos, false);
            }
        }
    }

    @Override
    public BlockState updatePostPlacement(BlockState state,
                                          Direction facing,
                                          BlockState facingState,
                                          IWorld world,
                                          BlockPos currentPos,
                                          BlockPos facingPos) {
        if (state.get(FLUIDLOGGED)) {
            BlockUtils.scheduleFluidTick(world, currentPos);
        }
        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }
}
