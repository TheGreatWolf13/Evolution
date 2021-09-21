package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.tileentities.ILoggable;
import tgw.evolution.blocks.tileentities.TEChopping;
import tgw.evolution.blocks.tileentities.TEUtils;
import tgw.evolution.entities.misc.EntitySit;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.items.ItemAxe;
import tgw.evolution.items.ItemLog;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.WoodVariant;

import javax.annotation.Nullable;

import static tgw.evolution.init.EvolutionBStates.FLUID_LOGGED;
import static tgw.evolution.init.EvolutionBStates.OCCUPIED;

public class BlockChopping extends BlockMass implements IReplaceable, ISittable, IFluidLoggable {

    public BlockChopping(WoodVariant name) {
        super(Properties.of(Material.WOOD).harvestLevel(HarvestLevel.STONE).sound(SoundType.WOOD).strength(8.0F, 2.0F), name.getMass() / 2);
        this.registerDefaultState(this.defaultBlockState().setValue(OCCUPIED, false).setValue(FLUID_LOGGED, false));
    }

    @Override
    public void attack(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (state.getValue(FLUID_LOGGED)) {
            return;
        }
        TileEntity tile = world.getBlockEntity(pos);
        if (!(tile instanceof TEChopping)) {
            return;
        }
        TEChopping chopping = (TEChopping) tile;
        if (chopping.hasLog() && player.getMainHandItem().getItem() instanceof ItemAxe) {
            if (chopping.increaseBreakProgress() == 3) {
                chopping.breakLog(player);
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
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        return BlockUtils.hasSolidSide(world, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(OCCUPIED, FLUID_LOGGED);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TEChopping();
    }

    @Override
    public NonNullList<ItemStack> getDrops(World world, BlockPos pos, BlockState state) {
        return NonNullList.of(ItemStack.EMPTY, new ItemStack(this));
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualEncouragement(state);
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
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
    public int getInitialAmount(BlockState state) {
        return 50_000;
    }

    @Override
    public int getMass(World world, BlockPos pos, BlockState state) {
        int mass = 0;
        if (state.getValue(FLUID_LOGGED)) {
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
        return EvolutionHitBoxes.SLAB_LOWER;
    }

    @Override
    public double getYOffset() {
        return 0.3;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            if (!state.canSurvive(world, pos)) {
                for (ItemStack stack : this.getDrops(world, pos, this.defaultBlockState())) {
                    popResource(world, pos, stack);
                }
                TEUtils.invokeIfInstance(world.getBlockEntity(pos), TEChopping::dropLog);
                world.removeBlock(pos, isMoving);
                return;
            }
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() == newState.getBlock()) {
            return;
        }
        TEUtils.invokeIfInstance(world.getBlockEntity(pos), TEChopping::dropLog);
    }

    @Override
    public void setBlockState(World world, BlockPos pos, BlockState state, @Nullable FluidGeneric fluid, int amount) {
        boolean hasFluid = amount > 0 && fluid != null;
        TileEntity tile = world.getBlockEntity(pos);
        if (hasFluid) {
            TEUtils.invokeIfInstance(tile, TEChopping::dropLog);
        }
        BlockState stateToPlace = state.setValue(FLUID_LOGGED, hasFluid);
        if (!(tile instanceof TEChopping)) {
            world.removeBlockEntity(pos);
        }
        world.setBlock(pos, stateToPlace, BlockFlags.NOTIFY_UPDATE_AND_RERENDER + BlockFlags.IS_MOVING);
        tile = world.getBlockEntity(pos);
        if (hasFluid) {
            TEUtils.<ILoggable>invokeIfInstance(tile, t -> t.setAmountAndFluid(amount, fluid), true);
            BlockUtils.scheduleFluidTick(world, pos);
        }
        else {
            TEUtils.<ILoggable>invokeIfInstance(tile, t -> t.setAmountAndFluid(0, null));
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(FLUID_LOGGED)) {
            BlockUtils.scheduleFluidTick(world, currentPos);
        }
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (state.getValue(FLUID_LOGGED)) {
            if (!state.getValue(OCCUPIED)) {
                if (EntitySit.create(world, pos, player)) {
                    world.setBlockAndUpdate(pos, state.setValue(OCCUPIED, true));
                    return ActionResultType.SUCCESS;
                }
            }
            return ActionResultType.PASS;
        }
        TileEntity tile = world.getBlockEntity(pos);
        if (!(tile instanceof TEChopping)) {
            return ActionResultType.PASS;
        }
        TEChopping chopping = (TEChopping) tile;
        if (player.getItemInHand(hand).getItem() instanceof ItemLog && !state.getValue(OCCUPIED)) {
            if (!chopping.hasLog()) {
                chopping.setStack(player, hand);
                return ActionResultType.SUCCESS;
            }
        }
        if (!chopping.hasLog() && !state.getValue(OCCUPIED)) {
            if (EntitySit.create(world, pos, player)) {
                world.setBlockAndUpdate(pos, state.setValue(OCCUPIED, true));
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.PASS;
        }
        if (chopping.hasLog()) {
            chopping.removeStack(player);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }
}
