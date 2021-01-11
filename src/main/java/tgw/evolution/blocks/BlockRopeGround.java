package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
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
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.init.EvolutionItems;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;

public class BlockRopeGround extends Block implements IReplaceable {

    public BlockRopeGround() {
        super(Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0).sound(SoundType.CLOTH).doesNotBlockMovement());
        this.setDefaultState(this.getDefaultState().with(DIRECTION_HORIZONTAL, Direction.NORTH));
    }

    public static boolean isSupported(IWorldReader world, BlockPos pos, Direction facing) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        for (int ropeCount = 1; ropeCount < 64; ropeCount++) {
            mutablePos = mutablePos.move(facing);
            BlockState currentState = world.getBlockState(mutablePos);
            if (currentState.getBlock() instanceof IRopeSupport) {
                if (!((IRopeSupport) currentState.getBlock()).canSupport(currentState, facing.getOpposite())) {
                    return false;
                }
                return ((IRopeSupport) currentState.getBlock()).getRopeLength() >= ropeCount;
            }
            if (currentState.getBlock() == EvolutionBlocks.GROUND_ROPE.get()) {
                if (currentState.get(DIRECTION_HORIZONTAL) == facing) {
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
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return new ItemStack(EvolutionItems.rope.get());
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(EvolutionItems.rope.get());
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return state.get(DIRECTION_HORIZONTAL).getAxis() == Direction.Axis.X ? EvolutionHitBoxes.ROPE_GROUND_X : EvolutionHitBoxes.ROPE_GROUND_Z;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        if (!isSupported(world, pos, state.get(DIRECTION_HORIZONTAL))) {
            return false;
        }
        return !world.isAirBlock(pos.down());
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
        }
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!world.isRemote) {
            Direction opposite = state.get(DIRECTION_HORIZONTAL).getOpposite();
            if (!(world.getBlockState(pos.offset(opposite)).getBlock() == this)) {
                BlockUtils.scheduleBlockTick(world, pos.down().offset(opposite), 2);
            }
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(DIRECTION_HORIZONTAL, rot.rotate(state.get(DIRECTION_HORIZONTAL)));
    }
}
