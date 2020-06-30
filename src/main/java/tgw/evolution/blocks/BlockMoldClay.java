package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;

public class BlockMoldClay extends Block implements IReplaceable {

    private static final VoxelShape[] SHAPES = {EvolutionHitBoxes.MOLD_1,
                                                EvolutionHitBoxes.MOLD_2,
                                                EvolutionHitBoxes.MOLD_3,
                                                EvolutionHitBoxes.MOLD_4,
                                                EvolutionHitBoxes.MOLD_5};
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final int layers;
    private VoxelShape shapeNorth;
    private VoxelShape shapeSouth;
    private VoxelShape shapeEast;
    private VoxelShape shapeWest;

    public BlockMoldClay(int layers) {
        super(Block.Properties.create(Material.CLAY).hardnessAndResistance(0F).sound(SoundType.GROUND));
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
        this.layers = layers;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return true;
    }

    public BlockMoldClay(VoxelShape shape) {
        this(0);
        this.shapeNorth = shape;
        this.shapeSouth = MathHelper.rotateShape(Direction.NORTH, Direction.SOUTH, shape);
        this.shapeWest = MathHelper.rotateShape(Direction.NORTH, Direction.WEST, shape);
        this.shapeEast = MathHelper.rotateShape(Direction.NORTH, Direction.EAST, shape);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote) {
            if (!state.isValidPosition(worldIn, pos)) {
                spawnDrops(state, worldIn, pos);
                worldIn.removeBlock(pos, false);
            }
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public ItemStack getDrops(BlockState state) {
        return new ItemStack(this);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (context.isPlacerSneaking()) {
            return null;
        }
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (this.layers != 0) {
            return SHAPES[this.layers - 1];
        }
        switch (state.get(FACING)) {
            case NORTH:
                return this.shapeNorth;
            case SOUTH:
                return this.shapeSouth;
            case EAST:
                return this.shapeEast;
        }
        return this.shapeWest;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockState down = worldIn.getBlockState(pos.down());
        BlockState up = worldIn.getBlockState(pos.up());
        return BlockUtils.isReplaceable(up) && Block.hasSolidSide(down, worldIn, pos.down(), Direction.UP);
    }
}
