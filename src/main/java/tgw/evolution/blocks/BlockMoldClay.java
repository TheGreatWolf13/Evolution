package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;

public class BlockMoldClay extends BlockGeneric implements IReplaceable {

    private final int layers;
    private VoxelShape shapeEast;
    private VoxelShape shapeNorth;
    private VoxelShape shapeSouth;
    private VoxelShape shapeWest;

    public BlockMoldClay(int layers) {
        super(Properties.of(Material.CLAY).strength(0.0F).sound(SoundType.GRAVEL));
        this.registerDefaultState(this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, Direction.NORTH));
        this.layers = layers;
    }

    public BlockMoldClay(VoxelShape shape) {
        this(0);
        this.shapeNorth = shape;
        this.shapeSouth = MathHelper.rotateShape(Direction.NORTH, Direction.SOUTH, shape);
        this.shapeWest = MathHelper.rotateShape(Direction.NORTH, Direction.WEST, shape);
        this.shapeEast = MathHelper.rotateShape(Direction.NORTH, Direction.EAST, shape);
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
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        BlockState up = world.getBlockState(pos.above());
        return BlockUtils.isReplaceable(up) && BlockUtils.hasSolidSide(world, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL);
    }

    @Override
    public NonNullList<ItemStack> getDrops(World world, BlockPos pos, BlockState state) {
        return NonNullList.of(new ItemStack(this));
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.45f;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        if (this.layers != 0) {
            return EvolutionHitBoxes.MOLD_CLAY[this.layers - 1];
        }
        switch (state.getValue(DIRECTION_HORIZONTAL)) {
            case NORTH: {
                return this.shapeNorth;
            }
            case SOUTH: {
                return this.shapeSouth;
            }
            case EAST: {
                return this.shapeEast;
            }
        }
        return this.shapeWest;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (context.isSecondaryUseActive()) {
            return null;
        }
        return this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, context.getHorizontalDirection());
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
}
