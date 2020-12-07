package tgw.evolution.blocks;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tgw.evolution.blocks.fluids.FluidGeneric;

public interface IBlockFluidContainer {

    int getAmountRemoved(World world, BlockPos pos, int maxAmount);

    FluidGeneric getFluid();
}
