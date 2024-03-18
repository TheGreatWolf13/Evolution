package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.DirectionUtil;

import java.util.List;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.*;

public class BlockClimbingStake extends BlockGeneric implements IReplaceable, IRopeSupport {

    public BlockClimbingStake() {
        super(Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.0f).noCollission());
        this.registerDefaultState(this.defaultBlockState()
                                      .setValue(EAST, false)
                                      .setValue(NORTH, false)
                                      .setValue(SOUTH, false)
                                      .setValue(WEST, false)
                                      .setValue(DOWN, false)
                                      .setValue(HIT, false)
                                      .setValue(DIRECTION_EXCEPT_UP, Direction.DOWN));
    }

    public static boolean canGoDown(BlockGetter level, BlockPos pos) {
        BlockPos posDown = pos.below();
        return BlockUtils.isReplaceable(level.getBlockState(posDown));
    }

    public static void checkSides(BlockState state, Level level, int x, int y, int z) {
        //TODO
        BlockPos pos = new BlockPos(x, y, z);
        for (Direction direction : DirectionUtil.HORIZ_NESW) {
            if (state.getValue(directionToProperty(direction))) {
                BlockState stateTest = level.getBlockState(pos.relative(direction));
                if (direction == Direction.DOWN) {
                    if (stateTest.getBlock() != EvolutionBlocks.ROPE) {
                        level.setBlockAndUpdate(pos, state.setValue(DOWN, false));
                    }
                    continue;
                }
                if (stateTest.getBlock() == EvolutionBlocks.ROPE_GROUND) {
                    if (stateTest.getValue(DIRECTION_HORIZONTAL).getOpposite() == direction) {
                        continue;
                    }
                }
                else if (BlockUtils.isReplaceable(stateTest)) {
                    stateTest = level.getBlockState(pos.relative(direction).below());
                    if (stateTest.getBlock() == EvolutionBlocks.ROPE) {
                        if (stateTest.getValue(DIRECTION_HORIZONTAL).getOpposite() == direction) {
                            continue;
                        }
                    }
                }
                level.setBlockAndUpdate(pos, state.setValue(directionToProperty(direction), false));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(EvolutionTexts.TOOLTIP_ROPE);
    }

    @Override
    public InteractionResult attack_(BlockState state, Level level, int x, int y, int z, Direction face, double hitX, double hitY, double hitZ, Player player) {
//        ItemStack mainhandStack = player.getMainHandItem();
//        if (player.isOnGround() && ItemUtils.isHammer(mainhandStack)) {
//            if (!state.getValue(HIT)) {
//                level.playSound(player, pos, SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 1.0f, 1.0f);
//                level.setBlockAndUpdate(pos, state.setValue(HIT, true));
//                mainhandStack.hurtAndBreak(1, player, playerEntity -> playerEntity.broadcastBreakEvent(InteractionHand.MAIN_HAND));
//            }
//            return;
//        }
//        if (!level.isClientSide) {
//            int rope = this.removeRope(state, level, pos);
//            while (rope > 64) {
//                //noinspection ObjectAllocationInLoop
//                BlockUtils.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(EvolutionItems.ROPE, 64));
//                rope -= 64;
//            }
//            BlockUtils.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(EvolutionItems.ROPE, rope));
//        }
//        level.removeBlock(pos, true);
//        dropResources(state, level, pos);
//        level.playSound(player, pos, SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.PASS;
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
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        Direction facing = state.getValue(DIRECTION_EXCEPT_UP);
        return BlockUtils.hasSolidFace(level, x + facing.getStepX(), y + facing.getStepY(), z + facing.getStepZ(), facing.getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_EXCEPT_UP, HIT, DOWN, EAST, NORTH, SOUTH, WEST);
    }

    @Override
    public int getRopeLength() {
        return 64;
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return switch (state.getValue(DIRECTION_EXCEPT_UP)) {
            case DOWN -> EvolutionShapes.TORCH;
            case NORTH -> EvolutionShapes.TORCH_SOUTH;
            case SOUTH -> EvolutionShapes.TORCH_NORTH;
            case WEST -> EvolutionShapes.TORCH_EAST;
            case EAST -> EvolutionShapes.TORCH_WEST;
            case UP -> throw new IllegalStateException();
        };
    }

    @Override
    public BlockState getStateForPlacement_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Direction face = hitResult.getDirection().getOpposite();
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
    public void neighborChanged_(BlockState state, Level level, int x, int y, int z, Block oldBlock, int fromX, int fromY, int fromZ, boolean b) {
        if (!level.isClientSide) {
            if (!state.canSurvive_(level, x, y, z)) {
                level.destroyBlock(new BlockPos(x, y, z), true);
                return;
            }
            checkSides(state, level, x, y, z);
        }
    }

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !isMoving && state.getBlock() != newState.getBlock()) {
            if (state.getValue(DIRECTION_EXCEPT_UP) == Direction.DOWN) {
//                BlockPos downPos = pos.below();
//                for (Direction direction : DirectionUtil.HORIZ_NESW) {
//                    if (state.getValue(directionToProperty(direction))) {
//                        BlockUtils.scheduleBlockTick(level, downPos.relative(direction));
//                    }
//                }
            }
        }
    }

    public int removeRope(BlockState state, Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        mutablePos.set(pos);
        BlockState temp;
        int count = 0;
        OList<BlockPos> toRemove = new OArrayList<>();
        if (state.getValue(DIRECTION_EXCEPT_UP) != Direction.DOWN) {
            for (int removingRopes = 1; removingRopes <= this.getRopeLength(); removingRopes++) {
                mutablePos.move(Direction.DOWN);
                temp = level.getBlockState(mutablePos);
                if (temp.getBlock() == EvolutionBlocks.ROPE) {
                    if (temp.getValue(DIRECTION_HORIZONTAL) == state.getValue(DIRECTION_EXCEPT_UP)) {
                        count++;
                        toRemove.add(mutablePos.immutable());
                        continue;
                    }
                }
                break;
            }
            for (BlockPos removing : toRemove) {
                level.removeBlock(removing, false);
            }
            return count;
        }
        for (Direction direction : DirectionUtil.HORIZ_NESW) {
            mutablePos.set(pos);
            if (state.getValue(directionToProperty(direction))) {
                Direction movement = direction;
                for (int removingRope = 1; removingRope <= this.getRopeLength(); removingRope++) {
                    mutablePos.move(movement);
                    temp = level.getBlockState(mutablePos);
                    if (movement != Direction.DOWN && temp.getBlock() == EvolutionBlocks.ROPE_GROUND) {
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
                        temp = level.getBlockState(mutablePos);
                        if (temp.getBlock() == EvolutionBlocks.ROPE) {
                            if (temp.getValue(DIRECTION_HORIZONTAL) == direction.getOpposite()) {
                                count++;
                                toRemove.add(mutablePos.immutable());
                                continue;
                            }
                        }
                        break;
                    }
                    if (movement == Direction.DOWN && temp.getBlock() == EvolutionBlocks.ROPE) {
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
        for (int i = toRemove.size() - 1; i >= 0; i--) {
            level.removeBlock(toRemove.get(i), false);
        }
        return count;
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        checkSides(state, level, x, y, z);
    }

    public int tryPlaceRopes(Level level, BlockPos pos, Direction movement, Direction support, int count) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        mutablePos.set(pos);
        Direction currentMovement = movement;
        int ropeCount = 0;
        for (int distance = 1; distance <= this.getRopeLength(); distance++) {
            if (ropeCount == count) {
                return count;
            }
            mutablePos.move(currentMovement);
            BlockState stateTemp = level.getBlockState(mutablePos);
            if (!BlockUtils.isReplaceable(stateTemp)) {
                return ropeCount;
            }
            if (currentMovement == Direction.DOWN && stateTemp.getBlock() == EvolutionBlocks.ROPE) {
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
            if (currentMovement != Direction.DOWN && stateTemp.getBlock() == EvolutionBlocks.ROPE_GROUND) {
                if (stateTemp.getValue(DIRECTION_HORIZONTAL) == support) {
                    continue;
                }
                return ropeCount;
            }
            if (currentMovement != Direction.DOWN && canGoDown(level, mutablePos)) {
                currentMovement = Direction.DOWN;
                stateTemp = level.getBlockState(mutablePos.move(Direction.DOWN));
                if (stateTemp.getBlock() == EvolutionBlocks.ROPE) {
                    if (stateTemp.getValue(DIRECTION_HORIZONTAL) == support) {
                        continue;
                    }
                    return ropeCount;
                }
                if (stateTemp.getBlock() == EvolutionBlocks.ROPE_GROUND) {
                    return ropeCount;
                }
//                if (stateTemp.getBlock() instanceof IReplaceable) {
//                    for (ItemStack stack : ((IReplaceable) stateTemp.getBlock()).getDrops(level, mutablePos, stateTemp)) {
//                        BlockUtils.dropItemStack(level, mutablePos, stack);
//                    }
//                }
                level.setBlockAndUpdate(mutablePos, EvolutionBlocks.ROPE.defaultBlockState().setValue(DIRECTION_HORIZONTAL, support));
                ropeCount++;
                continue;
            }
//            if (stateTemp.getBlock() instanceof IReplaceable) {
//                for (ItemStack stack : ((IReplaceable) stateTemp.getBlock()).getDrops(level, mutablePos, stateTemp)) {
//                    BlockUtils.dropItemStack(level, mutablePos, stack);
//                }
//            }
            if (currentMovement == Direction.DOWN) {
                level.setBlockAndUpdate(mutablePos, EvolutionBlocks.ROPE.defaultBlockState().setValue(DIRECTION_HORIZONTAL, support));
                ropeCount++;
                continue;
            }
            level.setBlockAndUpdate(mutablePos, EvolutionBlocks.ROPE_GROUND.defaultBlockState().setValue(DIRECTION_HORIZONTAL, support));
            ropeCount++;
        }
        return ropeCount;
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.getItem() == EvolutionItems.ROPE) {
            if (!state.getValue(HIT)) {
                player.displayClientMessage(EvolutionTexts.ACTION_HIT_STAKE, true);
                return InteractionResult.PASS;
            }
            int count = heldStack.getCount();
            Direction movement = state.getValue(DIRECTION_EXCEPT_UP) != Direction.DOWN ? Direction.DOWN : player.getDirection();
            Direction support = state.getValue(DIRECTION_EXCEPT_UP) == Direction.DOWN ?
                                player.getDirection().getOpposite() :
                                state.getValue(DIRECTION_EXCEPT_UP);
            Direction ropeDir = movement == Direction.DOWN ? Direction.DOWN : player.getDirection();
            boolean before = state.getValue(directionToProperty(ropeDir));
            BlockPos pos = new BlockPos(x, y, z);
            level.setBlockAndUpdate(pos, state.setValue(directionToProperty(ropeDir), true));
            int shrink = this.tryPlaceRopes(level, pos, movement, support, count);
            if (!player.isCreative()) {
                heldStack.shrink(shrink);
            }
            if (shrink > 0) {
                return InteractionResult.SUCCESS;
            }
            level.setBlockAndUpdate(pos, state.setValue(directionToProperty(ropeDir), before));
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }
}
