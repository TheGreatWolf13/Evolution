package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.*;
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
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.DirectionUtil;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.ATTACHED;
import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;

public class BlockClimbingHook extends BlockGeneric implements IReplaceable, IRopeSupport {

    public BlockClimbingHook() {
        super(Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.0f).noCollission().harvestLevel(HarvestLevel.UNBREAKABLE));
        this.registerDefaultState(this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, Direction.NORTH).setValue(ATTACHED, false));
    }

    public static void checkSides(BlockState state, World world, BlockPos pos) {
        Direction direction = state.getValue(DIRECTION_HORIZONTAL);
        BlockState stateTest = world.getBlockState(pos.relative(direction));
        if (stateTest.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
            if (DirectionUtil.getOpposite(stateTest.getValue(DIRECTION_HORIZONTAL)) == direction) {
                return;
            }
        }
        else if (BlockUtils.isReplaceable(stateTest)) {
            stateTest = world.getBlockState(pos.relative(direction).below());
            if (stateTest.getBlock() == EvolutionBlocks.ROPE.get()) {
                if (DirectionUtil.getOpposite(stateTest.getValue(DIRECTION_HORIZONTAL)) == direction) {
                    return;
                }
            }
        }
        world.setBlockAndUpdate(pos, state.setValue(ATTACHED, false));
    }

    @Override
    public void attack(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (!world.isClientSide) {
            int rope = this.removeRope(state, world, pos);
            BlockUtils.dropItemStack(world, pos, new ItemStack(EvolutionItems.rope.get(), rope));
        }
        world.removeBlock(pos, true);
        dropResources(state, world, pos);
        world.playSound(player, pos, SoundEvents.METAL_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
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
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        return BlockUtils.hasSolidSide(world, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL, ATTACHED);
    }

    @Override
    public NonNullList<ItemStack> getDrops(World world, BlockPos pos, BlockState state) {
        return NonNullList.of(ItemStack.EMPTY, new ItemStack(EvolutionItems.climbing_hook.get()));
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(EvolutionItems.climbing_hook.get());
    }

    @Override
    public int getRopeLength() {
        return 8;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.getValue(DIRECTION_HORIZONTAL)) {
            case NORTH: {
                return EvolutionHitBoxes.HOOK_NORTH;
            }
            case EAST: {
                return EvolutionHitBoxes.HOOK_EAST;
            }
            case SOUTH: {
                return EvolutionHitBoxes.HOOK_SOUTH;
            }
            case WEST: {
                return EvolutionHitBoxes.HOOK_WEST;
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
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            if (!state.canSurvive(world, pos)) {
                dropResources(state, world, pos);
                world.removeBlock(pos, false);
            }
            checkSides(state, world, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!world.isClientSide && !isMoving && state.getBlock() != newState.getBlock()) {
            BlockUtils.scheduleBlockTick(world, pos.below().relative(state.getValue(DIRECTION_HORIZONTAL)), BlockFlags.BLOCK_UPDATE);
        }
    }

    public int removeRope(BlockState state, World world, BlockPos pos) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        Direction direction = state.getValue(DIRECTION_HORIZONTAL);
        mutablePos.set(pos);
        Direction movement = direction;
        List<BlockPos> toRemove = new ArrayList<>();
        int count = 0;
        for (int removingRope = 1; removingRope <= this.getRopeLength(); removingRope++) {
            mutablePos.move(movement);
            BlockState temp = world.getBlockState(mutablePos);
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
                temp = world.getBlockState(mutablePos);
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
        MathHelper.iterateReverse(toRemove, removing -> world.removeBlock(removing, false));
        return count;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(DIRECTION_HORIZONTAL, rot.rotate(state.getValue(DIRECTION_HORIZONTAL)));
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!world.isClientSide) {
            checkSides(state, world, pos);
        }
    }
}
