package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.util.BlockUtils;
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
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return BlockUtils.isReplaceable(level.getBlockState_(x, y + 1, z)) && BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.6f;
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
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

    @Override
    public @Nullable BlockState getStateForPlacement_(Level level,
                                                      int x,
                                                      int y,
                                                      int z,
                                                      Player player,
                                                      InteractionHand hand,
                                                      BlockHitResult hitResult) {
        if (player.isSecondaryUseActive()) {
            return null;
        }
        return this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, player.getDirection());
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged_(BlockState state,
                                 Level level,
                                 int x,
                                 int y,
                                 int z,
                                 Block oldBlock,
                                 int fromX,
                                 int fromY,
                                 int fromZ,
                                 boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.canSurvive_(level, x, y, z)) {
                BlockPos pos = new BlockPos(x, y, z);
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
            }
        }
    }
}
