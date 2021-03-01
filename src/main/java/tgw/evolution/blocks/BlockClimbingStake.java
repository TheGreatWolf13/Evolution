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

public class BlockClimbingStake extends BlockEvolution implements IReplaceable, IRopeSupport {

    public BlockClimbingStake() {
        super(Properties.create(Material.IRON)
                        .sound(SoundType.METAL)
                        .hardnessAndResistance(0.0f)
                        .doesNotBlockMovement()
                        .harvestLevel(HarvestLevel.UNBREAKABLE));
        this.setDefaultState(this.getDefaultState()
                                 .with(EAST, false)
                                 .with(NORTH, false)
                                 .with(SOUTH, false)
                                 .with(WEST, false)
                                 .with(DOWN, false)
                                 .with(HIT, false)
                                 .with(DIRECTION_EXCEPT_UP, Direction.DOWN));
    }

    public static boolean canGoDown(World world, BlockPos pos) {
        BlockPos posDown = pos.down();
        return BlockUtils.isReplaceable(world.getBlockState(posDown));
    }

    public static void checkSides(BlockState state, World world, BlockPos pos) {
        for (Direction direction : MathHelper.DIRECTIONS_EXCEPT_UP) {
            if (state.get(directionToProperty(direction))) {
                BlockState stateTest = world.getBlockState(pos.offset(direction));
                if (direction == Direction.DOWN) {
                    if (stateTest.getBlock() != EvolutionBlocks.ROPE.get()) {
                        world.setBlockState(pos, state.with(DOWN, false));
                    }
                    continue;
                }
                if (stateTest.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                    if (stateTest.get(DIRECTION_HORIZONTAL).getOpposite() == direction) {
                        continue;
                    }
                }
                else if (BlockUtils.isReplaceable(stateTest)) {
                    stateTest = world.getBlockState(pos.offset(direction).down());
                    if (stateTest.getBlock() == EvolutionBlocks.ROPE.get()) {
                        if (stateTest.get(DIRECTION_HORIZONTAL).getOpposite() == direction) {
                            continue;
                        }
                    }
                }
                world.setBlockState(pos, state.with(directionToProperty(direction), false));
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(EvolutionTexts.TOOLTIP_ROPE);
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
        return state.get(directionToProperty(direction));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_EXCEPT_UP, HIT, DOWN, EAST, NORTH, SOUTH, WEST);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public int getRopeLength() {
        return 12;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        Direction dir = state.get(DIRECTION_EXCEPT_UP);
        switch (dir) {
            case DOWN:
                return EvolutionHitBoxes.TORCH;
            case NORTH:
                return EvolutionHitBoxes.TORCH_SOUTH;
            case SOUTH:
                return EvolutionHitBoxes.TORCH_NORTH;
            case WEST:
                return EvolutionHitBoxes.TORCH_EAST;
        }
        return EvolutionHitBoxes.TORCH_WEST;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction face = context.getFace().getOpposite();
        if (face == Direction.UP) {
            face = Direction.DOWN;
        }
        return this.getDefaultState().with(DIRECTION_EXCEPT_UP, face);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        Direction facing = state.get(DIRECTION_EXCEPT_UP);
        BlockPos posOffset = pos.offset(facing);
        BlockState stateFace = world.getBlockState(posOffset);
        return Block.hasSolidSide(stateFace, world, posOffset, facing.getOpposite());
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!world.isRemote) {
            if (!state.isValidPosition(world, pos)) {
                world.destroyBlock(pos, true);
                return;
            }
            checkSides(state, world, pos);
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack heldStack = player.getHeldItem(hand);
        if (heldStack.getItem() == EvolutionItems.rope.get()) {
            if (!state.get(HIT)) {
                player.sendStatusMessage(EvolutionTexts.ACTION_HIT_STAKE, true);
                return false;
            }
            int count = heldStack.getCount();
            Direction movement = state.get(DIRECTION_EXCEPT_UP) != Direction.DOWN ? Direction.DOWN : player.getHorizontalFacing();
            Direction support = state.get(DIRECTION_EXCEPT_UP) == Direction.DOWN ?
                                player.getHorizontalFacing().getOpposite() :
                                state.get(DIRECTION_EXCEPT_UP);
            Direction ropeDir = movement == Direction.DOWN ? Direction.DOWN : player.getHorizontalFacing();
            boolean before = state.get(directionToProperty(ropeDir));
            world.setBlockState(pos, state.with(directionToProperty(ropeDir), true));
            int shrink = this.tryPlaceRopes(world, pos, movement, support, count);
            heldStack.shrink(shrink);
            if (shrink > 0) {
                return true;
            }
            world.setBlockState(pos, state.with(directionToProperty(ropeDir), before));
            return false;
        }
        return false;
    }

    @Override
    public void onBlockClicked(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (player.onGround && player.getHeldItemMainhand().getItem() instanceof ItemHammer) {
            if (!state.get(HIT)) {
                world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.setBlockState(pos, state.with(HIT, true));
                player.getHeldItemMainhand().damageItem(1, player, playerEntity -> playerEntity.sendBreakAnimation(Hand.MAIN_HAND));
            }
            return;
        }
        if (!world.isRemote) {
            int rope = this.removeRope(state, world, pos);
            while (rope > 64) {
                //noinspection ObjectAllocationInLoop
                BlockUtils.dropItemStack(world, pos, new ItemStack(EvolutionItems.rope.get(), 64));
                rope -= 64;
            }
            BlockUtils.dropItemStack(world, pos, new ItemStack(EvolutionItems.rope.get(), rope));
        }
        world.removeBlock(pos, true);
        spawnDrops(state, world, pos);
        world.playSound(player, pos, SoundEvents.BLOCK_METAL_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!world.isRemote && !isMoving && state.getBlock() != newState.getBlock()) {
            if (state.get(DIRECTION_EXCEPT_UP) == Direction.DOWN) {
                BlockPos downPos = pos.down();
                for (Direction direction : MathHelper.DIRECTIONS_HORIZONTAL) {
                    if (state.get(directionToProperty(direction))) {
                        BlockUtils.scheduleBlockTick(world, downPos.offset(direction), 2);
                    }
                }
            }
        }
    }

    public int removeRope(BlockState state, World world, BlockPos pos) {
        List<BlockPos> toRemove = new ArrayList<>();
        int count = 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        BlockState temp;
        if (state.get(DIRECTION_EXCEPT_UP) != Direction.DOWN) {
            for (int removingRopes = 1; removingRopes <= this.getRopeLength(); removingRopes++) {
                mutablePos.move(Direction.DOWN);
                temp = world.getBlockState(mutablePos);
                if (temp.getBlock() == EvolutionBlocks.ROPE.get()) {
                    if (temp.get(DIRECTION_HORIZONTAL) == state.get(DIRECTION_EXCEPT_UP)) {
                        count++;
                        toRemove.add(mutablePos.toImmutable());
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
            mutablePos.setPos(pos);
            if (state.get(directionToProperty(direction))) {
                Direction movement = direction;
                for (int removingRope = 1; removingRope <= this.getRopeLength(); removingRope++) {
                    mutablePos.move(movement);
                    temp = world.getBlockState(mutablePos);
                    if (movement != Direction.DOWN && temp.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                        if (temp.get(DIRECTION_HORIZONTAL) == movement.getOpposite()) {
                            count++;
                            toRemove.add(mutablePos.toImmutable());
                            continue;
                        }
                        break;
                    }
                    if (movement != Direction.DOWN && BlockUtils.isReplaceable(temp)) {
                        movement = Direction.DOWN;
                        mutablePos.move(Direction.DOWN);
                        temp = world.getBlockState(mutablePos);
                        if (temp.getBlock() == EvolutionBlocks.ROPE.get()) {
                            if (temp.get(DIRECTION_HORIZONTAL) == direction.getOpposite()) {
                                count++;
                                toRemove.add(mutablePos.toImmutable());
                                continue;
                            }
                        }
                        break;
                    }
                    if (movement == Direction.DOWN && temp.getBlock() == EvolutionBlocks.ROPE.get()) {
                        if (temp.get(DIRECTION_HORIZONTAL) == direction.getOpposite()) {
                            count++;
                            toRemove.add(mutablePos.toImmutable());
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
    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (!worldIn.isRemote) {
            checkSides(state, worldIn, pos);
        }
    }

    public int tryPlaceRopes(World world, BlockPos pos, Direction movement, Direction support, int count) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
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
                if (stateTemp.get(DIRECTION_HORIZONTAL) == support) {
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
                if (stateTemp.get(DIRECTION_HORIZONTAL) == support) {
                    continue;
                }
                return ropeCount;
            }
            if (currentMovement != Direction.DOWN && canGoDown(world, mutablePos)) {
                currentMovement = Direction.DOWN;
                stateTemp = world.getBlockState(mutablePos.move(Direction.DOWN));
                if (stateTemp.getBlock() == EvolutionBlocks.ROPE.get()) {
                    if (stateTemp.get(DIRECTION_HORIZONTAL) == support) {
                        continue;
                    }
                    return ropeCount;
                }
                if (stateTemp.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                    return ropeCount;
                }
                if (stateTemp.getBlock() instanceof IReplaceable) {
                    BlockUtils.dropItemStack(world, mutablePos, ((IReplaceable) stateTemp.getBlock()).getDrops(world, mutablePos, stateTemp));
                }
                world.setBlockState(mutablePos, EvolutionBlocks.ROPE.get().getDefaultState().with(DIRECTION_HORIZONTAL, support));
                ropeCount++;
                continue;
            }
            if (stateTemp.getBlock() instanceof IReplaceable) {
                BlockUtils.dropItemStack(world, mutablePos, ((IReplaceable) stateTemp.getBlock()).getDrops(world, mutablePos, stateTemp));
            }
            if (currentMovement == Direction.DOWN) {
                world.setBlockState(mutablePos, EvolutionBlocks.ROPE.get().getDefaultState().with(DIRECTION_HORIZONTAL, support));
                ropeCount++;
                continue;
            }
            world.setBlockState(mutablePos, EvolutionBlocks.GROUND_ROPE.get().getDefaultState().with(DIRECTION_HORIZONTAL, support));
            ropeCount++;
        }
        return ropeCount;
    }
}
