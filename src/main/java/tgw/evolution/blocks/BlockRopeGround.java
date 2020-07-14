package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
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
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;

public class BlockRopeGround extends Block implements IReplaceable {

    public static final DirectionProperty ORIGIN = BlockStateProperties.HORIZONTAL_FACING;

    public BlockRopeGround() {
        super(Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0).sound(SoundType.CLOTH).doesNotBlockMovement());
        this.setDefaultState(this.getDefaultState().with(ORIGIN, Direction.NORTH));
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
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!worldIn.isRemote) {
            if (!(worldIn.getBlockState(pos.offset(state.get(ORIGIN).getOpposite())).getBlock() == this)) {
                BlockPos offsetPos = pos.down().offset(state.get(ORIGIN).getOpposite());
                worldIn.getPendingBlockTicks().scheduleTick(offsetPos, worldIn.getBlockState(offsetPos).getBlock(), 2);
            }
        }
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ORIGIN);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return state.get(ORIGIN).getAxis() == Direction.Axis.X ? EvolutionHitBoxes.ROPE_GROUND_X : EvolutionHitBoxes.ROPE_GROUND_Z;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        if (!isSupported(worldIn, pos, state.get(ORIGIN))) {
            return false;
        }
        return !worldIn.isAirBlock(pos.down());
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
