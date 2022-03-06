package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.tileentities.TELoggable;
import tgw.evolution.entities.misc.EntityFallingPeat;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionUtil;

import javax.annotation.Nullable;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.FLUID_LOGGED;
import static tgw.evolution.init.EvolutionBStates.LAYERS_1_4;

public class BlockPeat extends BlockMass implements IReplaceable, IFluidLoggable, EntityBlock {
    public BlockPeat() {
        super(Properties.of(Material.DIRT).strength(2.0f, 0.5f).sound(SoundType.GRAVEL), 1_156);
        this.registerDefaultState(this.defaultBlockState().setValue(LAYERS_1_4, 1).setValue(FLUID_LOGGED, false));
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
            BlockUtils.scheduleBlockTick(level, pos.relative(dir), 2);
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
            ((IReplaceable) state.getBlock()).onReplaced(state, level, pos);
            return;
        }
        if (state.getMaterial().isReplaceable()) {
            level.setBlock(pos, EvolutionBlocks.PEAT.get().defaultBlockState().setValue(LAYERS_1_4, layers), BlockFlags.NOTIFY_AND_UPDATE);
        }
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
    public boolean canFlowThrough(BlockState state, Direction direction) {
        if (state.getValue(LAYERS_1_4) == 4) {
            return direction == Direction.UP;
        }
        return direction != Direction.DOWN;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS_1_4, FLUID_LOGGED);
    }

    @Override
    public NonNullList<ItemStack> getDrops(Level level, BlockPos pos, BlockState state) {
        return NonNullList.of(ItemStack.EMPTY, new ItemStack(EvolutionItems.PEAT.get(), state.getValue(LAYERS_1_4)));
    }

    @Override
    public int getFluidCapacity(BlockState state) {
        int missingLayers = 4 - state.getValue(LAYERS_1_4);
        return missingLayers * 25_000;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.55f;
    }

    @Override
    public int getInitialAmount(BlockState state) {
        return state.getValue(LAYERS_1_4) * 25_000;
    }

    @Override
    public int getMass(Level level, BlockPos pos, BlockState state) {
        int mass = 0;
        if (state.getValue(FLUID_LOGGED)) {
            Fluid fluid = this.getFluid(level, pos);
            if (fluid instanceof FluidGeneric fluidGeneric) {
                int amount = this.getCurrentAmount(level, pos, state);
                int layers = Mth.ceil(amount / 12_500.0);
                mass = layers * fluidGeneric.getMass() / 8;
            }
        }
        return mass + this.getMass(state);
    }

    @Override
    public int getMass(BlockState state) {
        return state.getValue(LAYERS_1_4) * this.getBaseMass() / 4;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return EvolutionHitBoxes.PEAT[state.getValue(LAYERS_1_4)];
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
    public boolean isReplaceable(BlockState state) {
        return state.getValue(LAYERS_1_4) != 4;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TELoggable(pos, state);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        BlockUtils.scheduleBlockTick(level, pos.above(), 2);
        if (player.isCreative() || state.getValue(LAYERS_1_4) == 1) {
            return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        }
        this.playerWillDestroy(level, pos, state, player);
        level.setBlock(pos, state.setValue(LAYERS_1_4, state.getValue(LAYERS_1_4) - 1),
                       level.isClientSide ? BlockFlags.NOTIFY_UPDATE_AND_RERENDER : BlockFlags.NOTIFY_AND_UPDATE);
        return true;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        Block block = state.getBlock();
        Block newBlock = newState.getBlock();
        if (block == newBlock) {
            if (state.getValue(FLUID_LOGGED) && newState.getValue(FLUID_LOGGED)) {
                int layers = state.getValue(LAYERS_1_4);
                int newLayers = newState.getValue(LAYERS_1_4);
                if (newLayers > layers) {
                    Block fluidBlock = this.getFluidState(level, pos, state).createLegacyBlock().getBlock();
                    fluidBlock.onRemove(state, level, pos, newState, isMoving);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockUtils.scheduleBlockTick(level, pos.above(), 2);
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (state.getValue(FLUID_LOGGED)) {
            BlockUtils.scheduleFluidTick(level, pos);
        }
        checkFallable(level, pos, state);
    }

    @Override
    public BlockState updateShape(BlockState state,
                                  Direction facing,
                                  BlockState facingState,
                                  LevelAccessor level,
                                  BlockPos currentPos,
                                  BlockPos facingPos) {
        if (state.getValue(FLUID_LOGGED)) {
            BlockUtils.scheduleFluidTick(level, currentPos);
        }
        return !state.canSurvive(level, currentPos) ?
               Blocks.AIR.defaultBlockState() :
               super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }
}
