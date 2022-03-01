package tgw.evolution.blocks.tileentities;

import net.minecraft.world.level.material.Fluid;
import tgw.evolution.blocks.fluids.FluidGeneric;

import javax.annotation.Nullable;

public interface ILoggable {

    Fluid getFluid();

    int getFluidAmount();

    void setAmountAndFluid(int amount, @Nullable FluidGeneric fluid);

    void setFluidAmount(int amount);
}
