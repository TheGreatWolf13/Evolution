package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.tileentities.ILoggable;
import tgw.evolution.blocks.tileentities.TEChopping;
import tgw.evolution.entities.misc.EntitySittable;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.items.ItemLog;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.WoodVariant;

import static tgw.evolution.init.EvolutionBStates.FLUID_LOGGED;
import static tgw.evolution.init.EvolutionBStates.OCCUPIED;

public class BlockChopping extends BlockMass implements IReplaceable, ISittableBlock, IFluidLoggable, EntityBlock {

    public BlockChopping(WoodVariant name) {
        super(Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(8.0F, 2.0F), name.getMass() / 2);
        this.registerDefaultState(this.defaultBlockState().setValue(OCCUPIED, false).setValue(FLUID_LOGGED, false));
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (state.getValue(FLUID_LOGGED)) {
            return;
        }
        BlockEntity tile = level.getBlockEntity(pos);
        if (!(tile instanceof TEChopping chopping)) {
            return;
        }
        if (chopping.hasLog()) {
            ItemStack stackInHand = player.getMainHandItem();
            if (stackInHand.getItem() instanceof ItemModular tool && tool.isAxe(stackInHand)) {
                if (chopping.increaseBreakProgress() == 3) {
                    chopping.breakLog(player);
                }
            }
        }
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
    public boolean canFlowThrough(BlockState state, Direction direction) {
        return direction != Direction.DOWN;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return BlockUtils.hasSolidSide(level, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OCCUPIED, FLUID_LOGGED);
    }

    @Override
    public @Range(from = 0, to = 100) int getComfort() {
        //TODO implementation
        return 0;
    }

    @Override
    public NonNullList<ItemStack> getDrops(Level level, BlockPos pos, BlockState state) {
        return NonNullList.of(ItemStack.EMPTY, new ItemStack(this));
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualEncouragement(state);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualFlammability(state);
    }

    @Override
    public int getFluidCapacity(BlockState state) {
        return 50_000;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.62F;
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        return HarvestLevel.STONE;
    }

    @Override
    public int getInitialAmount(BlockState state) {
        return 50_000;
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
        return mass + this.getBaseMass();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return EvolutionShapes.SLAB_LOWER;
    }

    @Override
    public double getYOffset() {
        return 0.3;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.canSurvive(level, pos)) {
                for (ItemStack stack : this.getDrops(level, pos, this.defaultBlockState())) {
                    popResource(level, pos, stack);
                }
                if (level.getBlockEntity(pos) instanceof TEChopping te) {
                    te.dropLog();
                }
                level.removeBlock(pos, isMoving);
                return;
            }
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TEChopping(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() == newState.getBlock()) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof TEChopping te) {
            te.dropLog();
        }
    }

    @Override
    public void setBlockState(Level level, BlockPos pos, BlockState state, @Nullable FluidGeneric fluid, int amount) {
        boolean hasFluid = amount > 0 && fluid != null;
        BlockEntity tile = level.getBlockEntity(pos);
        if (hasFluid) {
            if (tile instanceof TEChopping te) {
                te.dropLog();
            }
        }
        BlockState stateToPlace = state.setValue(FLUID_LOGGED, hasFluid);
        if (!(tile instanceof TEChopping)) {
            level.removeBlockEntity(pos);
        }
        level.setBlock(pos, stateToPlace, BlockFlags.NOTIFY_UPDATE_AND_RERENDER + BlockFlags.IS_MOVING);
        tile = level.getBlockEntity(pos);
        if (hasFluid) {
            if (tile instanceof ILoggable te) {
                te.setAmountAndFluid(amount, fluid);
            }
            BlockUtils.scheduleFluidTick(level, pos);
        }
        else {
            if (tile instanceof ILoggable te) {
                te.setAmountAndFluid(0, null);
            }
        }
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
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(FLUID_LOGGED)) {
            if (!state.getValue(OCCUPIED)) {
                if (EntitySittable.create(level, pos, player)) {
                    level.setBlockAndUpdate(pos, state.setValue(OCCUPIED, true));
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        }
        BlockEntity tile = level.getBlockEntity(pos);
        if (!(tile instanceof TEChopping chopping)) {
            return InteractionResult.PASS;
        }
        if (player.getItemInHand(hand).getItem() instanceof ItemLog && !state.getValue(OCCUPIED)) {
            if (!chopping.hasLog()) {
                chopping.setStack(player, hand);
                return InteractionResult.SUCCESS;
            }
        }
        if (!chopping.hasLog() && !state.getValue(OCCUPIED)) {
            if (!player.isCrouching() && EntitySittable.create(level, pos, player)) {
                level.setBlockAndUpdate(pos, state.setValue(OCCUPIED, true));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (chopping.hasLog()) {
            chopping.removeStack(player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
