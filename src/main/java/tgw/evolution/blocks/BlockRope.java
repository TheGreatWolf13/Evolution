package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.init.EvolutionItems;

import javax.annotation.Nullable;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;

public class BlockRope extends BlockGeneric implements IReplaceable, IClimbable {

    public BlockRope() {
        super(Properties.of(Material.DECORATION).strength(0).sound(SoundType.WOOL).noCollission());
        this.registerDefaultState(this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, Direction.NORTH));
    }

    public static boolean isSupported(IWorldReader world, BlockPos pos, Direction facing) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        mutablePos.set(pos);
        Direction currentDirection = Direction.UP;
        for (int ropeCount = 1; ropeCount < 64; ropeCount++) {
            mutablePos.move(currentDirection);
            BlockState currentState = world.getBlockState(mutablePos);
            if (currentState.getBlock() instanceof IRopeSupport) {
                if (!((IRopeSupport) currentState.getBlock()).canSupport(currentState, currentDirection.getOpposite())) {
                    return false;
                }
                return ((IRopeSupport) currentState.getBlock()).getRopeLength() >= ropeCount;
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
            if (currentState.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
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
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        return isSupported(world, pos, state.getValue(DIRECTION_HORIZONTAL));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL);
    }

    @Override
    public Direction getDirection(BlockState state) {
        return state.getValue(DIRECTION_HORIZONTAL);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return new ItemStack(EvolutionItems.rope.get());
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.5f;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(EvolutionItems.rope.get());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.getValue(DIRECTION_HORIZONTAL)) {
            case NORTH:
                return EvolutionHitBoxes.ROPE_WALL_NORTH;
            case EAST:
                return EvolutionHitBoxes.ROPE_WALL_EAST;
            case SOUTH:
                return EvolutionHitBoxes.ROPE_WALL_SOUTH;
            case WEST:
                return EvolutionHitBoxes.ROPE_WALL_WEST;
        }
        throw new IllegalStateException("Could not find shape for " + state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
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
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
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
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            if (!state.canSurvive(world, pos)) {
                dropResources(state, world, pos);
                world.removeBlock(pos, false);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!world.isClientSide) {
            BlockPos support = pos.above().relative(state.getValue(DIRECTION_HORIZONTAL));
            Block block = world.getBlockState(support).getBlock();
            if (block instanceof IRopeSupport) {
                world.getBlockTicks().scheduleTick(support, block, 2);
            }
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(DIRECTION_HORIZONTAL, rot.rotate(state.getValue(DIRECTION_HORIZONTAL)));
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canSurvive(world, pos)) {
            dropResources(state, world, pos);
            world.removeBlock(pos, false);
        }
    }
}
