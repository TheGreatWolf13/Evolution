package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.tileentities.ILoggable;
import tgw.evolution.blocks.tileentities.TETorch;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.items.ItemTorch;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.time.Time;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.FLUID_LOGGED;
import static tgw.evolution.init.EvolutionBStates.LIT;

public class BlockTorch extends BlockMass implements IReplaceable, IFireSource, IFluidLoggable, EntityBlock {

    public BlockTorch() {
        super(Properties.of(Material.DECORATION).strength(0.0F).randomTicks().noCollission().sound(SoundType.WOOD), 0);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, true).setValue(FLUID_LOGGED, false));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
        if (!state.getValue(LIT)) {
            return;
        }
        double posX = pos.getX() + 0.5;
        double posY = pos.getY() + 0.7;
        double posZ = pos.getZ() + 0.5;
        level.addParticle(ParticleTypes.SMOKE, posX, posY, posZ, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, posX, posY, posZ, 0, 0, 0);
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return true;
    }

    @Override
    public boolean canFlowThrough(BlockState state, Direction direction) {
        return true;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, FLUID_LOGGED);
    }

    @Override
    public NonNullList<ItemStack> getDrops(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(LIT)) {
            ItemStack stack;
            if (level.getBlockEntity(pos) instanceof TETorch te) {
                stack = ItemTorch.getDroppedStack(te);
            }
            else {
                stack = ItemStack.EMPTY;
            }
            return NonNullList.of(ItemStack.EMPTY, stack);
        }
        return NonNullList.of(ItemStack.EMPTY, new ItemStack(EvolutionItems.torch_unlit.get()));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (!state.getValue(LIT)) {
            return Collections.singletonList(new ItemStack(EvolutionItems.torch_unlit.get()));
        }
        BlockEntity tile = builder.getParameter(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof TETorch teTorch) {
            return Collections.singletonList(ItemTorch.getDroppedStack(teTorch));
        }
        return Collections.singletonList(ItemStack.EMPTY);
    }

    @Override
    public int getFluidCapacity(BlockState state) {
        return 100_000;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0;
    }

    @Override
    public int getInitialAmount(BlockState state) {
        return 0;
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
        return EvolutionShapes.TORCH;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getFluidState(pos).isEmpty()) {
            return this.defaultBlockState().setValue(LIT, false);
        }
        if (CapabilityChunkStorage.contains(level.getChunkAt(pos), EnumStorage.OXYGEN, 1)) {
            return this.defaultBlockState();
        }
        return this.defaultBlockState().setValue(LIT, false);
    }

    @Override
    public boolean isFireSource(BlockState state) {
        return state.getValue(LIT);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(LIT);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TETorch(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (level.isClientSide) {
            return;
        }
        if (!state.getValue(LIT)) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof TETorch te) {
            te.setPlaceTime();
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (!state.getValue(LIT)) {
            return;
        }
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TETorch teTorch) {
            int torchTime = EvolutionConfig.SERVER.torchTime.get();
            if (torchTime == 0) {
                return;
            }
            if (level.getDayTime() >= teTorch.getTimePlaced() + (long) torchTime * Time.TICKS_PER_HOUR) {
                level.setBlockAndUpdate(pos, state.setValue(LIT, false));
            }
        }
    }

    @Override
    public void setBlockState(Level level, BlockPos pos, BlockState state, @Nullable FluidGeneric fluid, int amount) {
        boolean hasFluid = amount > 0 && fluid != null;
        if (hasFluid && state.getValue(LIT)) {
            level.levelEvent(LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0);
        }
        BlockState stateToPlace = state.setValue(FLUID_LOGGED, hasFluid).setValue(LIT, false);
        level.setBlock(pos, stateToPlace, BlockFlags.NOTIFY_UPDATE_AND_RERENDER);
        if (hasFluid) {
            if (level.getBlockEntity(pos) instanceof ILoggable te) {
                te.setAmountAndFluid(amount, fluid);
            }
            BlockUtils.scheduleFluidTick(level, pos);
        }
        else {
            if (level.getBlockEntity(pos) instanceof ILoggable te) {
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
        return facing == Direction.DOWN && !this.canSurvive(state, level, currentPos) ?
               Blocks.AIR.defaultBlockState() :
               super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(FLUID_LOGGED)) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() && state.getValue(LIT)) {
            level.setBlockAndUpdate(pos, state.setValue(LIT, false));
            level.playSound(player,
                            pos,
                            SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.BLOCKS,
                            1.0F,
                            2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
            return InteractionResult.SUCCESS;
        }
        if (stack.getItem() == EvolutionItems.torch.get() && !state.getValue(LIT)) {
            if (CapabilityChunkStorage.remove(level.getChunkAt(pos), EnumStorage.OXYGEN, 1)) {
                CapabilityChunkStorage.add(level.getChunkAt(pos), EnumStorage.CARBON_DIOXIDE, 1);
                level.setBlockAndUpdate(pos, state.setValue(LIT, true));
                level.playSound(player, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.7F + 0.3F);
                if (!level.isClientSide) {
                    if (level.getBlockEntity(pos) instanceof TETorch te) {
                        te.setPlaceTime();
                    }
                }
                player.awardStat(Stats.ITEM_CRAFTED.get(EvolutionItems.torch.get()));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }
}	
