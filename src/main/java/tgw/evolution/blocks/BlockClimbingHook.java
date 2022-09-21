package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.ATTACHED;
import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;

public class BlockClimbingHook extends BlockGeneric implements IReplaceable, IRopeSupport {

    public BlockClimbingHook() {
        super(Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.0f).noCollission());
        this.registerDefaultState(this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, Direction.NORTH).setValue(ATTACHED, false));
    }

    public static void checkSides(BlockState state, Level level, BlockPos pos) {
        Direction direction = state.getValue(DIRECTION_HORIZONTAL);
        BlockState stateTest = level.getBlockState(pos.relative(direction));
        if (stateTest.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
            if (DirectionUtil.getOpposite(stateTest.getValue(DIRECTION_HORIZONTAL)) == direction) {
                return;
            }
        }
        else if (BlockUtils.isReplaceable(stateTest)) {
            stateTest = level.getBlockState(pos.relative(direction).below());
            if (stateTest.getBlock() == EvolutionBlocks.ROPE.get()) {
                if (DirectionUtil.getOpposite(stateTest.getValue(DIRECTION_HORIZONTAL)) == direction) {
                    return;
                }
            }
        }
        level.setBlockAndUpdate(pos, state.setValue(ATTACHED, false));
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide) {
            int rope = this.removeRope(state, level, pos);
            BlockUtils.dropItemStack(level, pos, new ItemStack(EvolutionItems.rope.get(), rope));
        }
        level.removeBlock(pos, true);
        dropResources(state, level, pos);
        level.playSound(player, pos, SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return false;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Override
    public boolean canSupport(BlockState state, Direction direction) {
        return state.getValue(DIRECTION_HORIZONTAL) == direction;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return BlockUtils.hasSolidSide(level, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL, ATTACHED);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult hitResult, BlockGetter level, BlockPos pos, Player player) {
        return new ItemStack(EvolutionItems.climbing_hook.get());
    }

    @Override
    public NonNullList<ItemStack> getDrops(Level level, BlockPos pos, BlockState state) {
        return NonNullList.of(ItemStack.EMPTY, new ItemStack(EvolutionItems.climbing_hook.get()));
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0;
    }

    @Override
    public int getRopeLength() {
        return 8;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch (state.getValue(DIRECTION_HORIZONTAL)) {
            case NORTH -> {
                return EvolutionShapes.HOOK_NORTH;
            }
            case EAST -> {
                return EvolutionShapes.HOOK_EAST;
            }
            case SOUTH -> {
                return EvolutionShapes.HOOK_SOUTH;
            }
            case WEST -> {
                return EvolutionShapes.HOOK_WEST;
            }
        }
        throw new IllegalStateException("Invalid horizontal direction " + state.getValue(DIRECTION_HORIZONTAL));
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
            checkSides(state, level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !isMoving && state.getBlock() != newState.getBlock()) {
            BlockUtils.scheduleBlockTick(level, pos.below().relative(state.getValue(DIRECTION_HORIZONTAL)), BlockFlags.BLOCK_UPDATE);
        }
    }

    public int removeRope(BlockState state, Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        Direction direction = state.getValue(DIRECTION_HORIZONTAL);
        mutablePos.set(pos);
        Direction movement = direction;
        OList<BlockPos> toRemove = new OArrayList<>();
        int count = 0;
        for (int removingRope = 1; removingRope <= this.getRopeLength(); removingRope++) {
            mutablePos.move(movement);
            BlockState temp = level.getBlockState(mutablePos);
            if (movement != Direction.DOWN && temp.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                if (temp.getValue(DIRECTION_HORIZONTAL) == DirectionUtil.getOpposite(movement)) {
                    count++;
                    toRemove.add(mutablePos.immutable());
                    continue;
                }
                break;
            }
            if (movement != Direction.DOWN && BlockUtils.isReplaceable(temp)) {
                movement = Direction.DOWN;
                mutablePos.move(Direction.DOWN);
                temp = level.getBlockState(mutablePos);
                if (temp.getBlock() == EvolutionBlocks.ROPE.get()) {
                    if (temp.getValue(DIRECTION_HORIZONTAL) == DirectionUtil.getOpposite(direction)) {
                        count++;
                        toRemove.add(mutablePos.immutable());
                        continue;
                    }
                }
                break;
            }
            if (movement == Direction.DOWN && temp.getBlock() == EvolutionBlocks.ROPE.get()) {
                if (temp.getValue(DIRECTION_HORIZONTAL) == DirectionUtil.getOpposite(direction)) {
                    count++;
                    toRemove.add(mutablePos.immutable());
                    continue;
                }
            }
            break;
        }
        for (int i = toRemove.size() - 1; i >= 0; i--) {
            level.removeBlock(toRemove.get(i), false);
        }
        return count;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(DIRECTION_HORIZONTAL, rot.rotate(state.getValue(DIRECTION_HORIZONTAL)));
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (!level.isClientSide) {
            checkSides(state, level, pos);
        }
    }
}
