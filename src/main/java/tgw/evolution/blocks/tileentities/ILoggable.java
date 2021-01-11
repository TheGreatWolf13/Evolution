package tgw.evolution.blocks.tileentities;

import net.minecraft.fluid.Fluid;
import tgw.evolution.blocks.fluids.FluidGeneric;

import javax.annotation.Nullable;

public interface ILoggable {

    Fluid getFluid();

    int getFluidAmount();

    void setFluidAmount(int amount);

    void setAmountAndFluid(int amount, @Nullable FluidGeneric fluid);
}
