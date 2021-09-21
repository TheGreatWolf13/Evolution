package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.items.ItemHammer;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.*;

public class BlockClimbingStake extends BlockGeneric implements IReplaceable, IRopeSupport {

    public BlockClimbingStake() {
        super(Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.0f).noCollission().harvestLevel(HarvestLevel.UNBREAKABLE));
        this.registerDefaultState(this.defaultBlockState()
                                      .setValue(EAST, false)
                                      .setValue(NORTH, false)
                                      .setValue(SOUTH, false)
                                      .setValue(WEST, false)
                                      .setValue(DOWN, false)
                                      .setValue(HIT, false)
                                      .setValue(DIRECTION_EXCEPT_UP, Direction.DOWN));
    }

    public static boolean canGoDown(World world, BlockPos pos) {
        BlockPos posDown = pos.below();
        return BlockUtils.isReplaceable(world.getBlockState(posDown));
    }

    public static void checkSides(BlockState state, World world, BlockPos pos) {
        for (Direction direction : MathHelper.DIRECTIONS_EXCEPT_UP) {
            if (state.getValue(directionToProperty(direction))) {
                BlockState stateTest = world.getBlockState(pos.relative(direction));
                if (direction == Direction.DOWN) {
                    if (stateTest.getBlock() != EvolutionBlocks.ROPE.get()) {
                        world.setBlockAndUpdate(pos, state.setValue(DOWN, false));
                    }
                    continue;
                }
                if (stateTest.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                    if (stateTest.getValue(DIRECTION_HORIZONTAL).getOpposite() == direction) {
                        continue;
                    }
                }
                else if (BlockUtils.isReplaceable(stateTest)) {
                    stateTest = world.getBlockState(pos.relative(direction).below());
                    if (stateTest.getBlock() == EvolutionBlocks.ROPE.get()) {
                        if (stateTest.getValue(DIRECTION_HORIZONTAL).getOpposite() == direction) {
                            continue;
                        }
                    }
                }
                world.setBlockAndUpdate(pos, state.setValue(directionToProperty(direction), false));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(EvolutionTexts.TOOLTIP_ROPE);
    }

    @Override
    public void attack(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (player.isOnGround() && player.getMainHandItem().getItem() instanceof ItemHammer) {
            if (!state.getValue(HIT)) {
                world.playSound(player, pos, SoundEvents.ANVIL_HIT, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.setBlockAndUpdate(pos, state.setValue(HIT, true));
                player.getMainHandItem().hurtAndBreak(1, player, playerEntity -> playerEntity.broadcastBreakEvent(Hand.MAIN_HAND));
            }
            return;
        }
        if (!world.isClientSide) {
            int rope = this.removeRope(state, world, pos);
            while (rope > 64) {
                //noinspection ObjectAllocationInLoop
                BlockUtils.dropItemStack(world, pos, new ItemStack(EvolutionItems.rope.get(), 64));
                rope -= 64;
            }
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
        if (direction == Direction.UP) {
            return false;
        }
        return state.getValue(directionToProperty(direction));
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        Direction facing = state.getValue(DIRECTION_EXCEPT_UP);
        return BlockUtils.hasSolidSide(world, pos.relative(facing), facing.getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_EXCEPT_UP, HIT, DOWN, EAST, NORTH, SOUTH, WEST);
    }

    @Override
    public NonNullList<ItemStack> getDrops(World world, BlockPos pos, BlockState state) {
        return NonNullList.of(new ItemStack(this));
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0;
    }

    @Override
    public int getRopeLength() {
        return 64;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        Direction dir = state.getValue(DIRECTION_EXCEPT_UP);
        switch (dir) {
            case DOWN: {
                return EvolutionHitBoxes.TORCH;
            }
            case NORTH: {
                return EvolutionHitBoxes.TORCH_SOUTH;
            }
            case SOUTH: {
                return EvolutionHitBoxes.TORCH_NORTH;
            }
            case WEST: {
                return EvolutionHitBoxes.TORCH_EAST;
            }
        }
        return EvolutionHitBoxes.TORCH_WEST;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction face = context.getClickedFace().getOpposite();
        if (face == Direction.UP) {
            face = Direction.DOWN;
        }
        return this.defaultBlockState().setValue(DIRECTION_EXCEPT_UP, face);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            if (!state.canSurvive(world, pos)) {
                world.destroyBlock(pos, true);
                return;
            }
            checkSides(state, world, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!world.isClientSide && !isMoving && state.getBlock() != newState.getBlock()) {
            if (state.getValue(DIRECTION_EXCEPT_UP) == Direction.DOWN) {
                BlockPos downPos = pos.below();
                for (Direction direction : MathHelper.DIRECTIONS_HORIZONTAL) {
                    if (state.getValue(directionToProperty(direction))) {
                        BlockUtils.scheduleBlockTick(world, downPos.relative(direction), 2);
                    }
                }
            }
        }
    }

    public int removeRope(BlockState state, World world, BlockPos pos) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        mutablePos.set(pos);
        BlockState temp;
        int count = 0;
        List<BlockPos> toRemove = new ArrayList<>();
        if (state.getValue(DIRECTION_EXCEPT_UP) != Direction.DOWN) {
            for (int removingRopes = 1; removingRopes <= this.getRopeLength(); removingRopes++) {
                mutablePos.move(Direction.DOWN);
                temp = world.getBlockState(mutablePos);
                if (temp.getBlock() == EvolutionBlocks.ROPE.get()) {
                    if (temp.getValue(DIRECTION_HORIZONTAL) == state.getValue(DIRECTION_EXCEPT_UP)) {
                        count++;
                        toRemove.add(mutablePos.immutable());
                        continue;
                    }
                }
                break;
            }
            for (BlockPos removing : toRemove) {
                world.removeBlock(removing, false);
            }
            return count;
        }
        for (Direction direction : MathHelper.DIRECTIONS_HORIZONTAL) {
            mutablePos.set(pos);
            if (state.getValue(directionToProperty(direction))) {
                Direction movement = direction;
                for (int removingRope = 1; removingRope <= this.getRopeLength(); removingRope++) {
                    mutablePos.move(movement);
                    temp = world.getBlockState(mutablePos);
                    if (movement != Direction.DOWN && temp.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                        if (temp.getValue(DIRECTION_HORIZONTAL) == movement.getOpposite()) {
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
                            if (temp.getValue(DIRECTION_HORIZONTAL) == direction.getOpposite()) {
                                count++;
                                toRemove.add(mutablePos.immutable());
                                continue;
                            }
                        }
                        break;
                    }
                    if (movement == Direction.DOWN && temp.getBlock() == EvolutionBlocks.ROPE.get()) {
                        if (temp.getValue(DIRECTION_HORIZONTAL) == direction.getOpposite()) {
                            count++;
                            toRemove.add(mutablePos.immutable());
                            continue;
                        }
                    }
                    break;
                }
            }
        }
        MathHelper.iterateReverse(toRemove, removing -> world.removeBlock(removing, false));
        return count;
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        checkSides(state, world, pos);
    }

    public int tryPlaceRopes(World world, BlockPos pos, Direction movement, Direction support, int count) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        mutablePos.set(pos);
        Direction currentMovement = movement;
        int ropeCount = 0;
        for (int distance = 1; distance <= this.getRopeLength(); distance++) {
            if (ropeCount == count) {
                return count;
            }
            mutablePos.move(currentMovement);
            BlockState stateTemp = world.getBlockState(mutablePos);
            if (!BlockUtils.isReplaceable(stateTemp)) {
                return ropeCount;
            }
            if (currentMovement == Direction.DOWN && stateTemp.getBlock() == EvolutionBlocks.ROPE.get()) {
                if (stateTemp.getValue(DIRECTION_HORIZONTAL) == support) {
                    continue;
                }
                return ropeCount;
            }
            if (stateTemp.getBlock() instanceof IReplaceable) {
                if (!((IReplaceable) stateTemp.getBlock()).canBeReplacedByRope(stateTemp)) {
                    return ropeCount;
                }
            }
            if (currentMovement != Direction.DOWN && stateTemp.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                if (stateTemp.getValue(DIRECTION_HORIZONTAL) == support) {
                    continue;
                }
                return ropeCount;
            }
            if (currentMovement != Direction.DOWN && canGoDown(world, mutablePos)) {
                currentMovement = Direction.DOWN;
                stateTemp = world.getBlockState(mutablePos.move(Direction.DOWN));
                if (stateTemp.getBlock() == EvolutionBlocks.ROPE.get()) {
                    if (stateTemp.getValue(DIRECTION_HORIZONTAL) == support) {
                        continue;
                    }
                    return ropeCount;
                }
                if (stateTemp.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                    return ropeCount;
                }
                if (stateTemp.getBlock() instanceof IReplaceable) {
                    for (ItemStack stack : ((IReplaceable) stateTemp.getBlock()).getDrops(world, mutablePos, stateTemp)) {
                        BlockUtils.dropItemStack(world, mutablePos, stack);
                    }
                }
                world.setBlockAndUpdate(mutablePos, EvolutionBlocks.ROPE.get().defaultBlockState().setValue(DIRECTION_HORIZONTAL, support));
                ropeCount++;
                continue;
            }
            if (stateTemp.getBlock() instanceof IReplaceable) {
                for (ItemStack stack : ((IReplaceable) stateTemp.getBlock()).getDrops(world, mutablePos, stateTemp)) {
                    BlockUtils.dropItemStack(world, mutablePos, stack);
                }
            }
            if (currentMovement == Direction.DOWN) {
                world.setBlockAndUpdate(mutablePos, EvolutionBlocks.ROPE.get().defaultBlockState().setValue(DIRECTION_HORIZONTAL, support));
                ropeCount++;
                continue;
            }
            world.setBlockAndUpdate(mutablePos, EvolutionBlocks.GROUND_ROPE.get().defaultBlockState().setValue(DIRECTION_HORIZONTAL, support));
            ropeCount++;
        }
        return ropeCount;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.getItem() == EvolutionItems.rope.get()) {
            if (!state.getValue(HIT)) {
                player.displayClientMessage(EvolutionTexts.ACTION_HIT_STAKE, true);
                return ActionResultType.PASS;
            }
            int count = heldStack.getCount();
            Direction movement = state.getValue(DIRECTION_EXCEPT_UP) != Direction.DOWN ? Direction.DOWN : player.getDirection();
            Direction support = state.getValue(DIRECTION_EXCEPT_UP) == Direction.DOWN ?
                                player.getDirection().getOpposite() :
                                state.getValue(DIRECTION_EXCEPT_UP);
            Direction ropeDir = movement == Direction.DOWN ? Direction.DOWN : player.getDirection();
            boolean before = state.getValue(directionToProperty(ropeDir));
            world.setBlockAndUpdate(pos, state.setValue(directionToProperty(ropeDir), true));
            int shrink = this.tryPlaceRopes(world, pos, movement, support, count);
            if (!player.isCreative()) {
                heldStack.shrink(shrink);
            }
            if (shrink > 0) {
                return ActionResultType.SUCCESS;
            }
            world.setBlockAndUpdate(pos, state.setValue(directionToProperty(ropeDir), before));
            return ActionResultType.PASS;
        }
        return ActionResultType.PASS;
    }
}
