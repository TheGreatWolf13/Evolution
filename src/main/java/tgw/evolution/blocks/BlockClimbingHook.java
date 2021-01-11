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
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.ATTACHED;
import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;

public class BlockClimbingHook extends Block implements IReplaceable, IRopeSupport {

    public BlockClimbingHook() {
        super(Properties.create(Material.IRON)
                        .sound(SoundType.METAL)
                        .hardnessAndResistance(0.0f)
                        .doesNotBlockMovement()
                        .harvestLevel(HarvestLevel.UNBREAKABLE));
        this.setDefaultState(this.getDefaultState().with(DIRECTION_HORIZONTAL, Direction.NORTH).with(ATTACHED, false));
    }

    public static void checkSides(BlockState state, World world, BlockPos pos) {
        Direction direction = state.get(DIRECTION_HORIZONTAL);
        BlockState stateTest = world.getBlockState(pos.offset(direction));
        if (stateTest.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
            if (stateTest.get(DIRECTION_HORIZONTAL).getOpposite() == direction) {
                return;
            }
        }
        else if (BlockUtils.isReplaceable(stateTest)) {
            stateTest = world.getBlockState(pos.offset(direction).down());
            if (stateTest.getBlock() == EvolutionBlocks.ROPE.get()) {
                if (stateTest.get(DIRECTION_HORIZONTAL).getOpposite() == direction) {
                    return;
                }
            }
        }
        world.setBlockState(pos, state.with(ATTACHED, false));
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
        return state.get(DIRECTION_HORIZONTAL) == direction;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL, ATTACHED);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return new ItemStack(EvolutionItems.climbing_hook.get());
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public int getRopeLength() {
        return 5;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.get(DIRECTION_HORIZONTAL)) {
            case NORTH:
                return EvolutionHitBoxes.HOOK_NORTH;
            case EAST:
                return EvolutionHitBoxes.HOOK_EAST;
            case SOUTH:
                return EvolutionHitBoxes.HOOK_SOUTH;
            case WEST:
                return EvolutionHitBoxes.HOOK_WEST;
        }
        throw new IllegalStateException("Invalid horizontal direction " + state.get(DIRECTION_HORIZONTAL));
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        BlockPos posOffset = pos.offset(Direction.DOWN);
        BlockState stateFace = world.getBlockState(posOffset);
        return Block.hasSolidSide(stateFace, world, posOffset, Direction.UP);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.with(DIRECTION_HORIZONTAL, mirror.mirror(state.get(DIRECTION_HORIZONTAL)));
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isRemote) {
            if (!state.isValidPosition(world, pos)) {
                spawnDrops(state, world, pos);
                world.removeBlock(pos, false);
            }
            checkSides(state, world, pos);
        }
    }

    @Override
    public void onBlockClicked(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (!world.isRemote) {
            int rope = this.removeRope(state, world, pos);
            BlockUtils.dropItemStack(world, pos, new ItemStack(EvolutionItems.rope.get(), rope));
        }
        world.removeBlock(pos, true);
        spawnDrops(state, world, pos);
        world.playSound(player, pos, SoundEvents.BLOCK_METAL_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!world.isRemote && !isMoving && state.getBlock() != newState.getBlock()) {
            BlockUtils.scheduleBlockTick(world, pos.down().offset(state.get(DIRECTION_HORIZONTAL)), BlockFlags.BLOCK_UPDATE);
        }
    }

    public int removeRope(BlockState state, World world, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        Direction direction = state.get(DIRECTION_HORIZONTAL);
        mutablePos.setPos(pos);
        Direction movement = direction;
        List<BlockPos> toRemove = new ArrayList<>();
        int count = 0;
        for (int removingRope = 1; removingRope <= this.getRopeLength(); removingRope++) {
            mutablePos.move(movement);
            BlockState temp = world.getBlockState(mutablePos);
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
        MathHelper.iterateReverse(toRemove, removing -> world.removeBlock(removing, false));
        return count;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(DIRECTION_HORIZONTAL, rot.rotate(state.get(DIRECTION_HORIZONTAL)));
    }

    @Override
    public void tick(BlockState state, World world, BlockPos pos, Random random) {
        if (!world.isRemote) {
            checkSides(state, world, pos);
        }
    }
}
