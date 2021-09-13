package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.tileentities.ILoggable;
import tgw.evolution.blocks.tileentities.TETorch;
import tgw.evolution.blocks.tileentities.TEUtils;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.items.ItemTorch;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.Time;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.FLUIDLOGGED;
import static tgw.evolution.init.EvolutionBStates.LIT;

public class BlockTorch extends BlockMass implements IReplaceable, IFireSource, IFluidLoggable {

    public BlockTorch() {
        super(Properties.of(Material.DECORATION).strength(0.0F).randomTicks().noCollission().sound(SoundType.WOOD), 0);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, true).setValue(FLUIDLOGGED, false));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (!state.getValue(LIT)) {
            return;
        }
        double posX = pos.getX() + 0.5;
        double posY = pos.getY() + 0.7;
        double posZ = pos.getZ() + 0.5;
        world.addParticle(ParticleTypes.SMOKE, posX, posY, posZ, 0, 0, 0);
        world.addParticle(ParticleTypes.FLAME, posX, posY, posZ, 0, 0, 0);
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
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        return canSupportCenter(world, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(LIT, FLUIDLOGGED);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TETorch();
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        if (state.getValue(LIT)) {
            return TEUtils.returnIfInstance(world.getBlockEntity(pos), ItemTorch::getDroppedStack, ItemStack.EMPTY);
        }
        return new ItemStack(EvolutionItems.torch_unlit.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (!state.getValue(LIT)) {
            return Collections.singletonList(new ItemStack(EvolutionItems.torch_unlit.get()));
        }
        TileEntity tile = builder.getParameter(LootParameters.BLOCK_ENTITY);
        if (tile instanceof TETorch) {
            return Collections.singletonList(ItemTorch.getDroppedStack((TETorch) tile));
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
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

    @Override
    public int getMass(World world, BlockPos pos, BlockState state) {
        int mass = 0;
        if (state.getValue(FLUIDLOGGED)) {
            Fluid fluid = this.getFluid(world, pos);
            if (fluid instanceof FluidGeneric) {
                int amount = this.getCurrentAmount(world, pos, state);
                int layers = MathHelper.ceil(amount / 12_500.0);
                mass = layers * ((FluidGeneric) fluid).getMass() / 8;
            }
        }
        return mass + this.getBaseMass();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return EvolutionHitBoxes.TORCH;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!world.getFluidState(pos).isEmpty()) {
            return this.defaultBlockState().setValue(LIT, false);
        }
        if (CapabilityChunkStorage.contains(world.getChunkAt(pos), EnumStorage.OXYGEN, 1)) {
            return this.defaultBlockState();
        }
        return this.defaultBlockState().setValue(LIT, false);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getValue(FLUIDLOGGED) || state.getValue(LIT);
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

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (world.isClientSide) {
            return;
        }
        if (!state.getValue(LIT)) {
            return;
        }
        TEUtils.invokeIfInstance(world.getBlockEntity(pos), TETorch::setPlaceTime);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.getValue(LIT)) {
            return;
        }
        TileEntity tile = world.getBlockEntity(pos);
        if (tile != null) {
            TETorch teTorch = (TETorch) tile;
            int torchTime = EvolutionConfig.COMMON.torchTime.get();
            if (torchTime == 0) {
                return;
            }
            if (world.getDayTime() >= teTorch.getTimePlaced() + (long) torchTime * Time.HOUR_IN_TICKS) {
                world.setBlockAndUpdate(pos, state.setValue(LIT, false));
            }
        }
    }

    @Override
    public void setBlockState(World world, BlockPos pos, BlockState state, @Nullable FluidGeneric fluid, int amount) {
        boolean hasFluid = amount > 0 && fluid != null;
        if (hasFluid && state.getValue(LIT)) {
            world.levelEvent(Constants.WorldEvents.FIRE_EXTINGUISH_SOUND, pos, 0);
        }
        BlockState stateToPlace = state.setValue(FLUIDLOGGED, hasFluid).setValue(LIT, false);
        world.setBlock(pos, stateToPlace, BlockFlags.NOTIFY_UPDATE_AND_RERENDER);
        if (hasFluid) {
            TEUtils.<ILoggable>invokeIfInstance(world.getBlockEntity(pos), t -> t.setAmountAndFluid(amount, fluid), true);
            BlockUtils.scheduleFluidTick(world, pos);
        }
        else {
            TEUtils.<ILoggable>invokeIfInstance(world.getBlockEntity(pos), t -> t.setAmountAndFluid(0, null));
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(FLUIDLOGGED)) {
            BlockUtils.scheduleFluidTick(world, currentPos);
        }
        return facing == Direction.DOWN && !this.canSurvive(state, world, currentPos) ?
               Blocks.AIR.defaultBlockState() :
               super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        Evolution.LOGGER.debug("{}", world.getBlockEntity(pos));
        if (state.getValue(FLUIDLOGGED)) {
            return ActionResultType.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() && state.getValue(LIT)) {
            world.setBlockAndUpdate(pos, state.setValue(LIT, false));
            world.playSound(player,
                            pos,
                            SoundEvents.FIRE_EXTINGUISH,
                            SoundCategory.BLOCKS,
                            1.0F,
                            2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
            return ActionResultType.SUCCESS;
        }
        if (stack.getItem() == EvolutionItems.torch.get() && !state.getValue(LIT)) {
            if (CapabilityChunkStorage.remove(world.getChunkAt(pos), EnumStorage.OXYGEN, 1)) {
                CapabilityChunkStorage.add(world.getChunkAt(pos), EnumStorage.CARBON_DIOXIDE, 1);
                world.setBlockAndUpdate(pos, state.setValue(LIT, true));
                world.playSound(player, pos, SoundEvents.FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.7F + 0.3F);
                if (!world.isClientSide) {
                    TEUtils.invokeIfInstance(world.getBlockEntity(pos), TETorch::setPlaceTime);
                }
                player.awardStat(Stats.ITEM_CRAFTED.get(EvolutionItems.torch.get()));
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.PASS;
        }
        return ActionResultType.PASS;
    }
}	
