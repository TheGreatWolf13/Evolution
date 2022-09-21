package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.util.math.MathHelper;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;

public class BlockMoldClay extends BlockGeneric implements IReplaceable {

    private final int layers;
    private VoxelShape shapeEast = Shapes.empty();
    private VoxelShape shapeNorth = Shapes.empty();
    private VoxelShape shapeSouth = Shapes.empty();
    private VoxelShape shapeWest = Shapes.empty();

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
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState up = level.getBlockState(pos.above());
        return BlockUtils.isReplaceable(up) && BlockUtils.hasSolidSide(level, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL);
    }

    @Override
    public NonNullList<ItemStack> getDrops(Level level, BlockPos pos, BlockState state) {
        return NonNullList.of(ItemStack.EMPTY, new ItemStack(this));
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.45f;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (this.layers != 0) {
            return EvolutionShapes.MOLD_CLAY[this.layers - 1];
        }
        return switch (state.getValue(DIRECTION_HORIZONTAL)) {
            case NORTH -> this.shapeNorth;
            case SOUTH -> this.shapeSouth;
            case EAST -> this.shapeEast;
            default -> this.shapeWest;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
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
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.canSurvive(level, pos)) {
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
            }
        }
    }
}
