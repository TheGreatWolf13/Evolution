package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionShapes;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;

public class BlockRope extends BlockGeneric implements IReplaceable, IFallSufixBlock {

    public BlockRope() {
        super(Properties.of(Material.DECORATION).strength(0).sound(SoundType.WOOL).noCollission());
        this.registerDefaultState(this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, Direction.NORTH));
    }

    public static boolean isSupported(BlockGetter level, int x, int y, int z, Direction facing) {
        Direction currentDirection = Direction.UP;
        for (int ropeCount = 1; ropeCount < 64; ropeCount++) {
            switch (currentDirection) {
                case UP -> ++y;
                case DOWN -> --y;
                case EAST -> ++x;
                case WEST -> --x;
                case NORTH -> --z;
                case SOUTH -> ++z;
            }
            BlockState currentState = level.getBlockState_(x, y, z);
            if (currentState.getBlock() instanceof IRopeSupport ropeSupport) {
                if (!ropeSupport.canSupport(currentState, currentDirection.getOpposite())) {
                    return false;
                }
                return ropeSupport.getRopeLength() >= ropeCount;
            }
            if (currentDirection == Direction.UP && currentState.getBlock() == EvolutionBlocks.ROPE) {
                if (currentState.getValue(DIRECTION_HORIZONTAL) == facing) {
                    continue;
                }
                return false;
            }
            if (currentDirection == Direction.UP && BlockUtils.isReplaceable(currentState)) {
                currentDirection = facing;
                ropeCount--;
                continue;
            }
            if (currentState.getBlock() == EvolutionBlocks.ROPE_GROUND) {
                if (currentState.getValue(DIRECTION_HORIZONTAL) == facing) {
                    continue;
                }
                return false;
            }
            break;
        }
        return false;
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return isSupported(level, x, y, z, state.getValue(DIRECTION_HORIZONTAL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, int x, int y, int z, Player player) {
        return new ItemStack(EvolutionItems.ROPE);
    }

    @Override
    public Direction getDirection(BlockState state) {
        return state.getValue(DIRECTION_HORIZONTAL);
    }

    @Override
    public String getFallSuffix() {
        return "rope";
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.6f;
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return switch (state.getValue(DIRECTION_HORIZONTAL)) {
            case NORTH -> EvolutionShapes.ROPE_WALL_NORTH;
            case EAST -> EvolutionShapes.ROPE_WALL_EAST;
            case SOUTH -> EvolutionShapes.ROPE_WALL_SOUTH;
            default -> EvolutionShapes.ROPE_WALL_WEST;
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
        return this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, player.getDirection());
    }

    @Override
    public float getSweepAngle() {
        return 140;
    }

    @Override
    public double getUpSpeed() {
        return 0.05;
    }

    @Override
    public double getXPos(BlockState state) {
        Direction facing = state.getValue(DIRECTION_HORIZONTAL);
        if (facing.getAxis() == Direction.Axis.X) {
            return 2 / 16.0 * facing.getStepX();
        }
        return Double.NaN;
    }

    @Override
    public double getZPos(BlockState state) {
        Direction facing = state.getValue(DIRECTION_HORIZONTAL);
        if (facing.getAxis() == Direction.Axis.Z) {
            return 2 / 16.0 * facing.getStepZ();
        }
        return Double.NaN;
    }

    @Override
    public boolean isClimbable(BlockState state, LevelReader level, int x, int y, int z, Entity entity) {
        return true;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(DIRECTION_HORIZONTAL, mirror.mirror(state.getValue(DIRECTION_HORIZONTAL)));
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

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (!level.isClientSide) {
//            BlockPos support = pos.above().relative(state.getValue(DIRECTION_HORIZONTAL));
//            Block block = level.getBlockState(support).getBlock();
//            if (block instanceof IRopeSupport) {
//                level.scheduleTick(support, block, 2);
//            }
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(DIRECTION_HORIZONTAL, rot.rotate(state.getValue(DIRECTION_HORIZONTAL)));
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (!state.canSurvive_(level, x, y, z)) {
            BlockUtils.dropResources(state, level, x, y, z);
            level.removeBlock_(x, y, z, false);
        }
    }
}
