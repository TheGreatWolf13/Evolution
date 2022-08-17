package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.tileentities.ILoggable;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.util.constants.BlockFlags;

import javax.annotation.Nullable;

public interface IFluidLoggable extends IBlockFluidContainer {

    boolean canFlowThrough(BlockState state, Direction direction);

    @Override
    default int getAmountRemoved(Level level, BlockPos pos, int maxAmount) {
        int currentAmount = 0;
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof ILoggable loggable) {
            currentAmount = loggable.getFluidAmount();
        }
        if (currentAmount == 0) {
            return 0;
        }
        int removed = Math.min(maxAmount, currentAmount);
        currentAmount -= removed;
        Fluid fluid = this.getFluid(level, pos);
        this.setBlockState(level, pos, level.getBlockState(pos), fluid instanceof FluidGeneric ? (FluidGeneric) fluid : null, currentAmount);
        return removed;
    }

    default int getApparentAmount(BlockState state, ILoggable tile) {
        int amountAtPos = tile.getFluidAmount();
        amountAtPos += this.getInitialAmount(state);
        return amountAtPos;
    }

    default int getApparentAmount(Level level, BlockPos pos, BlockState state) {
        if (!state.getValue(EvolutionBStates.FLUID_LOGGED)) {
            return this.getInitialAmount(state);
        }
        BlockEntity tile = level.getBlockEntity(pos);
        int amountAtPos = 0;
        if (tile instanceof ILoggable loggable) {
            amountAtPos = loggable.getFluidAmount();
        }
        amountAtPos += this.getInitialAmount(state);
        return amountAtPos;
    }

    default int getApparentLayers(Level level, BlockPos pos, BlockState state) {
        int apparent = this.getApparentAmount(level, pos, state);
        return Math.min(Mth.ceil(apparent / 12_500.0), 8);
    }

    default int getApparentLayers(BlockState state, ILoggable tile) {
        int apparent = this.getApparentAmount(state, tile);
        return Math.min(Mth.ceil(apparent / 12_500.0), 8);
    }

    default int getCurrentAmount(Level level, BlockPos pos, BlockState state) {
        if (!state.getValue(EvolutionBStates.FLUID_LOGGED)) {
            return 0;
        }
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof ILoggable loggable) {
            return loggable.getFluidAmount();
        }
        return 0;
    }

    @Override
    default Fluid getFluid(BlockGetter world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ILoggable loggable) {
            return loggable.getFluid();
        }
        return Fluids.EMPTY;
    }

    int getFluidCapacity(BlockState state);

    default FluidState getFluidState(Level world, BlockPos pos, BlockState state) {
        if (!state.getValue(EvolutionBStates.FLUID_LOGGED)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ILoggable loggable) {
            Fluid fluid = loggable.getFluid();
            if (fluid == Fluids.EMPTY) {
                return Fluids.EMPTY.defaultFluidState();
            }
            int amount = this.getApparentAmount(state, loggable);
            int layers = this.getApparentLayers(state, loggable);
            boolean full = layers * 12_500 == amount;
            return fluid.defaultFluidState().setValue(FluidGeneric.LEVEL, layers).setValue(FluidGeneric.FALLING, full);
        }
        return Fluids.EMPTY.defaultFluidState();
    }

    int getInitialAmount(BlockState state);

    default boolean isFull(Level level, BlockPos pos, BlockState state) {
        return this.getFluidCapacity(state) == this.getCurrentAmount(level, pos, state);
    }

    @Override
    default int receiveFluid(Level level, BlockPos pos, BlockState state, FluidGeneric fluid, int amount) {
        if (fluid.isEquivalentOrEmpty(level, pos)) {
            int currentFluid = 0;
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof ILoggable loggable) {
                currentFluid = loggable.getFluidAmount();
            }
            int total = Math.min(currentFluid + amount, this.getFluidCapacity(state));
            this.setBlockState(level, pos, state, fluid, total);
            return total - currentFluid;
        }
        Evolution.warn("Fluids are different, handle them!");
        return 0;
    }

    default boolean remove(Level level, BlockPos pos, BlockState state) {
        Fluid fluid = this.getFluid(level, pos);
        if (fluid instanceof FluidGeneric fluidGeneric) {
            int amount = this.getCurrentAmount(level, pos, state);
            fluidGeneric.setBlockStateInternal(level, pos, amount);
            return true;
        }
        return false;
    }

    default void setBlockState(Level level, BlockPos pos, BlockState state, @Nullable FluidGeneric fluid, int amount) {
        boolean hasFluid = amount > 0 && fluid != null;
        BlockState stateToPlace = state.setValue(EvolutionBStates.FLUID_LOGGED, hasFluid);
        if (!(level.getBlockEntity(pos) instanceof ILoggable)) {
            level.removeBlockEntity(pos);
        }
        level.setBlock(pos, stateToPlace, BlockFlags.NOTIFY_UPDATE_AND_RERENDER + BlockFlags.IS_MOVING);
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
}
