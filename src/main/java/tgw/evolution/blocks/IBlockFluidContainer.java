package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import tgw.evolution.blocks.fluids.FluidGeneric;

public interface IBlockFluidContainer {

    int getAmountRemoved(World world, BlockPos pos, int maxAmount);

    Fluid getFluid(IBlockReader world, BlockPos pos);

    int receiveFluid(World world, BlockPos pos, BlockState state, FluidGeneric fluid, int amount);
}
