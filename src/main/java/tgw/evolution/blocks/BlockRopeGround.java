package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;

public class BlockRopeGround extends BlockGeneric implements IReplaceable {

    public BlockRopeGround() {
        super(Properties.of(Material.DECORATION).strength(0).sound(SoundType.WOOL).noCollission());
        this.registerDefaultState(this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, Direction.NORTH));
    }

    public static boolean isSupported(BlockGetter level, BlockPos pos, Direction facing) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        mutablePos.set(pos);
        for (int ropeCount = 1; ropeCount < 64; ropeCount++) {
            mutablePos = mutablePos.move(facing);
            BlockState currentState = level.getBlockState(mutablePos);
            if (currentState.getBlock() instanceof IRopeSupport) {
                if (!((IRopeSupport) currentState.getBlock()).canSupport(currentState, facing.getOpposite())) {
                    return false;
                }
                return ((IRopeSupport) currentState.getBlock()).getRopeLength() >= ropeCount;
            }
            if (currentState.getBlock() == EvolutionBlocks.ROPE_GROUND.get()) {
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
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        if (!isSupported(world, pos, state.getValue(DIRECTION_HORIZONTAL))) {
            return false;
        }
        return !world.isEmptyBlock(pos.below());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return new ItemStack(EvolutionItems.ROPE.get());
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.5f;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return state.getValue(DIRECTION_HORIZONTAL).getAxis() == Direction.Axis.X ? EvolutionShapes.ROPE_GROUND_X : EvolutionShapes.ROPE_GROUND_Z;
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
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide) {
            Direction opposite = state.getValue(DIRECTION_HORIZONTAL).getOpposite();
            if (!(level.getBlockState(pos.relative(opposite)).getBlock() == this)) {
                BlockUtils.scheduleBlockTick(level, pos.getX() + opposite.getStepX(), pos.getY() - 1, pos.getZ() + opposite.getStepZ());
            }
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(DIRECTION_HORIZONTAL, rot.rotate(state.getValue(DIRECTION_HORIZONTAL)));
    }
}
