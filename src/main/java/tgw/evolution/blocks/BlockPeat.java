package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.entities.misc.EntityFallingPeat;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.LAYERS_1_4;

public class BlockPeat extends BlockPhysics implements IReplaceable, IAir {
    public BlockPeat() {
        super(Properties.of(Material.DIRT).strength(2.0f, 0.5f).sound(SoundType.GRAVEL));
        this.registerDefaultState(this.defaultBlockState().setValue(LAYERS_1_4, 1));
    }

    private static boolean canFallThrough(BlockState state, BlockGetter level, BlockPos pos) {
        if (!BlockGravity.canFallThrough(state)) {
            return false;
        }
        if (state.getBlock() instanceof BlockPeat) {
            return state.getValue(LAYERS_1_4) != 4;
        }
        return state.getCollisionShape(level, pos).isEmpty();
    }

    public static void checkFallable(Level level, BlockPos pos, BlockState state) {
        BlockPos posDown = pos.below();
        BlockState stateDown = level.getBlockState(posDown);
        int layers = 0;
        if (stateDown.getBlock() == EvolutionBlocks.PEAT.get()) {
            layers = stateDown.getValue(LAYERS_1_4);
        }
        if (layers == 4) {
            return;
        }
        if (!level.isEmptyBlock(posDown)) {
            if (!canFallThrough(stateDown, level, posDown)) {
                return;
            }
        }
        if (pos.getY() < 0) {
            return;
        }
        level.removeBlock(pos, true);
        EntityFallingPeat entity = new EntityFallingPeat(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, state.getValue(LAYERS_1_4));
        level.addFreshEntity(entity);
        entity.playSound(EvolutionSounds.SOIL_COLLAPSE.get(), 0.25F, 1.0F);
        for (Direction dir : DirectionUtil.ALL_EXCEPT_DOWN) {
            BlockUtils.scheduleBlockTick(level, pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());
        }
    }

    public static void placeLayersOn(Level level, BlockPos pos, int layers) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() == EvolutionBlocks.PEAT.get()) {
            for (int i = 1; i <= 4; i++) {
                if (state.getValue(LAYERS_1_4) == i) {
                    if (i + layers > 4) {
                        int remain = i + layers - 4;
                        level.setBlock(pos, state.setValue(LAYERS_1_4, 4), BlockFlags.NOTIFY_AND_UPDATE);
                        level.setBlock(pos.above(), EvolutionBlocks.PEAT.get().defaultBlockState().setValue(LAYERS_1_4, remain),
                                       BlockFlags.NOTIFY_AND_UPDATE);
                        return;
                    }
                    level.setBlock(pos, state.setValue(LAYERS_1_4, i + layers), BlockFlags.NOTIFY_AND_UPDATE);
                    return;
                }
            }
        }
        if (state.getBlock() instanceof IReplaceable) {
            level.setBlock(pos, EvolutionBlocks.PEAT.get().defaultBlockState().setValue(LAYERS_1_4, layers), BlockFlags.NOTIFY_AND_UPDATE);
            return;
        }
        if (state.getMaterial().isReplaceable()) {
            level.setBlock(pos, EvolutionBlocks.PEAT.get().defaultBlockState().setValue(LAYERS_1_4, layers), BlockFlags.NOTIFY_AND_UPDATE);
        }
    }

    @Override
    public boolean allowsFrom(BlockState state, Direction from) {
        if (from == Direction.DOWN) {
            return false;
        }
        return state.getValue(LAYERS_1_4) != 4;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (context.getItemInHand().getItem() == this.asItem() && state.getValue(LAYERS_1_4) < 4) {
            if (context.replacingClickedOnBlock()) {
                return context.getClickedFace() == Direction.UP;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return state.getValue(LAYERS_1_4) < 4;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS_1_4);
    }

    @Override
    public @Nullable SoundEvent fallingSound() {
        return EvolutionSounds.SOIL_COLLAPSE.get();
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.63f;
    }

    @Override
    public double getMass(Level level, BlockPos pos, BlockState state) {
        return state.getValue(LAYERS_1_4) * (1_156 / 4.0);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return EvolutionShapes.SLAB_4_D[state.getValue(LAYERS_1_4) - 1];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (state.getBlock() == this) {
            int layers = state.getValue(LAYERS_1_4);
            return state.setValue(LAYERS_1_4, Math.min(layers + 1, 4));
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public @Range(from = 1, to = 31) int increment(BlockState state, Direction from) {
        return 1;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return state.getValue(LAYERS_1_4) != 4;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        BlockUtils.scheduleBlockTick(level, pos.getX(), pos.getY() + 1, pos.getZ());
        if (player.isCreative() || state.getValue(LAYERS_1_4) == 1) {
            return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        }
        this.playerWillDestroy(level, pos, state, player);
        level.setBlock(pos, state.setValue(LAYERS_1_4, state.getValue(LAYERS_1_4) - 1),
                       level.isClientSide ? BlockFlags.NOTIFY_UPDATE_AND_RERENDER : BlockFlags.NOTIFY_AND_UPDATE);
        return true;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockUtils.scheduleBlockTick(level, pos.getX(), pos.getY() + 1, pos.getZ());
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        checkFallable(level, pos, state);
    }

    @Override
    public BlockState updateShape(BlockState state,
                                  Direction direction,
                                  BlockState fromState,
                                  LevelAccessor level,
                                  BlockPos pos,
                                  BlockPos fromPos) {
        return !state.canSurvive(level, pos) ?
               Blocks.AIR.defaultBlockState() :
               super.updateShape(state, direction, fromState, level, pos, fromPos);
    }
}
