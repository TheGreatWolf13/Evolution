package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tgw.evolution.blocks.fluids.FluidGeneric;

public interface IBlockFluidContainer {

    int getAmountRemoved(World world, BlockPos pos, int maxAmount);

    FluidGeneric getFluid();

    int receiveFluid(World world, BlockPos pos, BlockState state, Fluid fluid, int amount);
}
