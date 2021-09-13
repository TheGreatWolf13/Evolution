package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.tileentities.ILoggable;
import tgw.evolution.blocks.tileentities.TEUtils;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;

public interface IFluidLoggable extends IBlockFluidContainer {

    boolean canFlowThrough(BlockState state, Direction direction);

    @Override
    default int getAmountRemoved(World world, BlockPos pos, int maxAmount) {
        int currentAmount = 0;
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ILoggable) {
            currentAmount = ((ILoggable) tile).getFluidAmount();
        }
        if (currentAmount == 0) {
            return 0;
        }
        int removed = MathHelper.clampMax(maxAmount, currentAmount);
        currentAmount -= removed;
        Fluid fluid = this.getFluid(world, pos);
        this.setBlockState(world, pos, world.getBlockState(pos), fluid instanceof FluidGeneric ? (FluidGeneric) fluid : null, currentAmount);
        return removed;
    }

    default int getApparentAmount(BlockState state, ILoggable tile) {
        int amountAtPos = tile.getFluidAmount();
        amountAtPos += this.getInitialAmount(state);
        return amountAtPos;
    }

    default int getApparentAmount(World world, BlockPos pos, BlockState state) {
        if (!state.getValue(EvolutionBStates.FLUIDLOGGED)) {
            return this.getInitialAmount(state);
        }
        TileEntity tile = world.getBlockEntity(pos);
        int amountAtPos = 0;
        if (tile instanceof ILoggable) {
            amountAtPos = ((ILoggable) tile).getFluidAmount();
        }
        amountAtPos += this.getInitialAmount(state);
        return amountAtPos;
    }

    default int getApparentLayers(World world, BlockPos pos, BlockState state) {
        int apparent = this.getApparentAmount(world, pos, state);
        return MathHelper.clampMax(MathHelper.ceil(apparent / 12_500.0), 8);
    }

    default int getApparentLayers(BlockState state, ILoggable tile) {
        int apparent = this.getApparentAmount(state, tile);
        return MathHelper.clampMax(MathHelper.ceil(apparent / 12_500.0), 8);
    }

    default int getCurrentAmount(World world, BlockPos pos, BlockState state) {
        if (!state.getValue(EvolutionBStates.FLUIDLOGGED)) {
            return 0;
        }
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ILoggable) {
            return ((ILoggable) tile).getFluidAmount();
        }
        return 0;
    }

    @Override
    default Fluid getFluid(IBlockReader world, BlockPos pos) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ILoggable) {
            return ((ILoggable) tile).getFluid();
        }
        return Fluids.EMPTY;
    }

    int getFluidCapacity(BlockState state);

    default FluidState getFluidState(World world, BlockPos pos, BlockState state) {
        if (!state.getValue(EvolutionBStates.FLUIDLOGGED)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ILoggable) {
            Fluid fluid = ((ILoggable) tile).getFluid();
            if (fluid == Fluids.EMPTY) {
                return Fluids.EMPTY.defaultFluidState();
            }
            int amount = this.getApparentAmount(state, (ILoggable) tile);
            int layers = this.getApparentLayers(state, (ILoggable) tile);
            boolean full = layers * 12_500 == amount;
            return fluid.defaultFluidState().setValue(FluidGeneric.LEVEL, layers).setValue(FluidGeneric.FALLING, full);
        }
        return Fluids.EMPTY.defaultFluidState();
    }

    int getInitialAmount(BlockState state);

    default boolean isFull(World world, BlockPos pos, BlockState state) {
        return this.getFluidCapacity(state) == this.getCurrentAmount(world, pos, state);
    }

    @Override
    default int receiveFluid(World world, BlockPos pos, BlockState state, FluidGeneric fluid, int amount) {
        if (fluid.isEquivalentOrEmpty(world, pos)) {
            int currentFluid = 0;
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof ILoggable) {
                currentFluid = ((ILoggable) tile).getFluidAmount();
            }
            int total = MathHelper.clampMax(currentFluid + amount, this.getFluidCapacity(state));
            this.setBlockState(world, pos, state, fluid, total);
            return total - currentFluid;
        }
        Evolution.LOGGER.warn("Fluids are different, handle them!");
        return 0;
    }

    default boolean remove(World world, BlockPos pos, BlockState state) {
        Fluid fluid = this.getFluid(world, pos);
        if (fluid instanceof FluidGeneric) {
            int amount = this.getCurrentAmount(world, pos, state);
            ((FluidGeneric) fluid).setBlockStateInternal(world, pos, amount);
            return true;
        }
        return false;
    }

    default void setBlockState(World world, BlockPos pos, BlockState state, @Nullable FluidGeneric fluid, int amount) {
        boolean hasFluid = amount > 0 && fluid != null;
        BlockState stateToPlace = state.setValue(EvolutionBStates.FLUIDLOGGED, hasFluid);
        if (!(world.getBlockEntity(pos) instanceof ILoggable)) {
            world.removeBlockEntity(pos);
        }
        world.setBlock(pos, stateToPlace, BlockFlags.NOTIFY_UPDATE_AND_RERENDER + BlockFlags.IS_MOVING);
        if (hasFluid) {
            TEUtils.<ILoggable>invokeIfInstance(world.getBlockEntity(pos), t -> t.setAmountAndFluid(amount, fluid), true);
            BlockUtils.scheduleFluidTick(world, pos);
        }
        else {
            TEUtils.<ILoggable>invokeIfInstance(world.getBlockEntity(pos), t -> t.setAmountAndFluid(0, null));
        }
    }
}
