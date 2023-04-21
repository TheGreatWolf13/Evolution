package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
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

    public static boolean isSupported(BlockGetter level, BlockPos pos, Direction facing) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        mutablePos.set(pos);
        Direction currentDirection = Direction.UP;
        for (int ropeCount = 1; ropeCount < 64; ropeCount++) {
            mutablePos.move(currentDirection);
            BlockState currentState = level.getBlockState(mutablePos);
            if (currentState.getBlock() instanceof IRopeSupport ropeSupport) {
                if (!ropeSupport.canSupport(currentState, currentDirection.getOpposite())) {
                    return false;
                }
                return ropeSupport.getRopeLength() >= ropeCount;
            }
            if (currentDirection == Direction.UP && currentState.getBlock() == EvolutionBlocks.ROPE.get()) {
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
            if (currentState.getBlock() == EvolutionBlocks.ROPE_GROUND.get()) {
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
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isSupported(level, pos, state.getValue(DIRECTION_HORIZONTAL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return new ItemStack(EvolutionItems.ROPE.get());
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(DIRECTION_HORIZONTAL)) {
            case NORTH -> EvolutionShapes.ROPE_WALL_NORTH;
            case EAST -> EvolutionShapes.ROPE_WALL_EAST;
            case SOUTH -> EvolutionShapes.ROPE_WALL_SOUTH;
            case WEST -> EvolutionShapes.ROPE_WALL_WEST;
            default -> throw new IllegalStateException("Could not find shape for " + state);
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, context.getHorizontalDirection());
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
    public boolean isClimbable(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
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
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.canSurvive(level, pos)) {
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide) {
            BlockPos support = pos.above().relative(state.getValue(DIRECTION_HORIZONTAL));
            Block block = level.getBlockState(support).getBlock();
            if (block instanceof IRopeSupport) {
                level.scheduleTick(support, block, 2);
            }
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(DIRECTION_HORIZONTAL, rot.rotate(state.getValue(DIRECTION_HORIZONTAL)));
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (!state.canSurvive(level, pos)) {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }
}
