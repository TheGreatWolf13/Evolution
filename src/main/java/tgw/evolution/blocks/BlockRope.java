package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockRope extends Block implements IReplaceable {

    public static final EnumProperty<Direction> DIRECTION = BlockStateProperties.HORIZONTAL_FACING;

    public BlockRope() {
        super(Properties.create(Material.WOOL).hardnessAndResistance(0).sound(SoundType.CLOTH).doesNotBlockMovement());
        this.setDefaultState(this.getDefaultState().with(DIRECTION, Direction.NORTH));
    }

    public static boolean isSupported(IWorldReader world, BlockPos pos, Direction facing) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        Direction currentDirection = Direction.UP;
        for (int ropeCount = 1; ropeCount < 64; ropeCount++) {
            mutablePos = mutablePos.move(currentDirection);
            BlockState currentState = world.getBlockState(mutablePos);
            if (currentState.getBlock() instanceof IRopeSupport) {
                if (!((IRopeSupport) currentState.getBlock()).canSupport(currentState, currentDirection.getOpposite())) {
                    return false;
                }
                return ((IRopeSupport) currentState.getBlock()).getRopeLength() >= ropeCount;
            }
            if (currentDirection == Direction.UP && currentState.getBlock() == EvolutionBlocks.ROPE.get()) {
                if (currentState.get(DIRECTION) == facing) {
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
                if (currentState.get(BlockRopeGround.ORIGIN) == facing) {
                    continue;
                }
                return false;
            }
            break;
        }
        return false;
    }

    @Override
    public boolean canBeReplacedByLiquid(BlockState state) {
        return true;
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (!worldIn.isRemote) {
            if (!state.isValidPosition(worldIn, pos)) {
                spawnDrops(state, worldIn, pos);
                worldIn.removeBlock(pos, false);
            }
        }
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return isSupported(worldIn, pos, state.get(DIRECTION));
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!worldIn.isRemote) {
            BlockPos support = pos.up().offset(state.get(DIRECTION));
            Evolution.LOGGER.debug("support = " + support);
            Block block = worldIn.getBlockState(support).getBlock();
            Evolution.LOGGER.debug("block = " + block);
            if (block instanceof IRopeSupport) {
                worldIn.getPendingBlockTicks().scheduleTick(support, block, 2);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote) {
            if (!state.isValidPosition(worldIn, pos)) {
                spawnDrops(state, worldIn, pos);
                worldIn.removeBlock(pos, false);
            }
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(DIRECTION, context.getPlacementHorizontalFacing());
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        switch (state.get(DIRECTION)) {
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

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(EvolutionItems.rope.get());
    }

    @Override
    public ItemStack getDrops(BlockState state) {
        return new ItemStack(EvolutionItems.rope.get());
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }
}
