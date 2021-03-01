package tgw.evolution.blocks;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
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
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.tileentities.ILoggable;
import tgw.evolution.blocks.tileentities.TETorch;
import tgw.evolution.blocks.tileentities.TEUtils;
import tgw.evolution.capabilities.chunkstorage.ChunkStorageCapability;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.items.ItemTorch;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.Time;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.FLUIDLOGGED;
import static tgw.evolution.init.EvolutionBStates.LIT;

public class BlockTorch extends BlockMass implements IReplaceable, IFireSource, IFluidLoggable {

    public BlockTorch() {
        super(Block.Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0F).tickRandomly().doesNotBlockMovement().sound(SoundType.WOOD),
              0);
        this.setDefaultState(this.getDefaultState().with(LIT, true).with(FLUIDLOGGED, false));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (!state.get(LIT)) {
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
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TETorch();
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(LIT, FLUIDLOGGED);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        if (state.get(LIT)) {
            return TEUtils.returnIfInstance(world.getTileEntity(pos), ItemTorch::getDroppedStack, ItemStack.EMPTY);
        }
        return new ItemStack(EvolutionItems.torch_unlit.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (!state.get(LIT)) {
            List<ItemStack> list = new ArrayList<>(1);
            list.add(new ItemStack(EvolutionItems.torch_unlit.get()));
            return list;
        }
        TileEntity tile = builder.get(LootParameters.BLOCK_ENTITY);
        if (tile instanceof TETorch) {
            List<ItemStack> list = new ArrayList<>(1);
            list.add(ItemTorch.getDroppedStack((TETorch) tile));
            return list;
        }
        return Lists.newArrayList(ItemStack.EMPTY);
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
    public int getLightValue(BlockState state) {
        return state.get(LIT) ? 15 : 0;
    }

    @Override
    public int getMass(World world, BlockPos pos, BlockState state) {
        int mass = 0;
        if (state.get(FLUIDLOGGED)) {
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
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return EvolutionHitBoxes.TORCH;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        if (!world.getFluidState(pos).isEmpty()) {
            return this.getDefaultState().with(LIT, false);
        }
        if (ChunkStorageCapability.contains(world.getChunkAt(pos), EnumStorage.OXYGEN, 1)) {
            return this.getDefaultState();
        }
        return this.getDefaultState().with(LIT, false);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.get(FLUIDLOGGED) || state.get(LIT);
    }

    @Override
    public boolean isFireSource(BlockState state) {
        return state.get(LIT);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        return hasEnoughSolidSide(world, pos.down(), Direction.UP);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        Evolution.LOGGER.debug("{}", world.getTileEntity(pos));
        if (state.get(FLUIDLOGGED)) {
            return false;
        }
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty() && state.get(LIT)) {
            world.setBlockState(pos, state.with(LIT, false));
            world.playSound(player,
                            pos,
                            SoundEvents.BLOCK_FIRE_EXTINGUISH,
                            SoundCategory.BLOCKS,
                            1.0F,
                            2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
            return true;
        }
        if (stack.getItem() == EvolutionItems.torch.get() && !state.get(LIT)) {
            if (ChunkStorageCapability.remove(world.getChunkAt(pos), EnumStorage.OXYGEN, 1)) {
                ChunkStorageCapability.add(world.getChunkAt(pos), EnumStorage.CARBON_DIOXIDE, 1);
                world.setBlockState(pos, state.with(LIT, true));
                world.playSound(player, pos, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.7F + 0.3F);
                if (!world.isRemote) {
                    TEUtils.invokeIfInstance(world.getTileEntity(pos), TETorch::setPlaceTime);
                }
                player.addStat(Stats.ITEM_CRAFTED.get(EvolutionItems.torch.get()));
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (world.isRemote) {
            return;
        }
        if (!state.get(LIT)) {
            return;
        }
        TEUtils.invokeIfInstance(world.getTileEntity(pos), TETorch::setPlaceTime);
    }

    @Override
    public void randomTick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.isRemote || !state.get(LIT)) {
            return;
        }
        if (world.getTileEntity(pos) != null) {
            TETorch tile = (TETorch) world.getTileEntity(pos);
            int torchTime = EvolutionConfig.COMMON.torchTime.get();
            if (torchTime == 0) {
                return;
            }
            if (world.getDayTime() >= tile.getTimePlaced() + (long) torchTime * Time.HOUR_IN_TICKS) {
                world.setBlockState(pos, state.with(LIT, false));
            }
        }
    }

    @Override
    public void setBlockState(World world, BlockPos pos, BlockState state, @Nullable FluidGeneric fluid, int amount) {
        boolean hasFluid = amount > 0 && fluid != null;
        if (hasFluid && state.get(LIT)) {
            world.playEvent(Constants.WorldEvents.FIRE_EXTINGUISH_SOUND, pos, 0);
        }
        BlockState stateToPlace = state.with(FLUIDLOGGED, hasFluid).with(LIT, false);
        world.setBlockState(pos, stateToPlace, BlockFlags.NOTIFY_UPDATE_AND_RERENDER);
        if (hasFluid) {
            TEUtils.<ILoggable>invokeIfInstance(world.getTileEntity(pos), t -> t.setAmountAndFluid(amount, fluid), true);
            BlockUtils.scheduleFluidTick(world, pos);
        }
        else {
            TEUtils.<ILoggable>invokeIfInstance(world.getTileEntity(pos), t -> t.setAmountAndFluid(0, null));
        }
    }

    @Override
    public boolean ticksRandomly(BlockState state) {
        return state.get(LIT);
    }

    @Override
    public BlockState updatePostPlacement(BlockState state,
                                          Direction facing,
                                          BlockState facingState,
                                          IWorld world,
                                          BlockPos currentPos,
                                          BlockPos facingPos) {
        if (state.get(FLUIDLOGGED)) {
            BlockUtils.scheduleFluidTick(world, currentPos);
        }
        return facing == Direction.DOWN && !this.isValidPosition(state, world, currentPos) ?
               Blocks.AIR.getDefaultState() :
               super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }
}	
