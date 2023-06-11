package tgw.evolution.mixin;

import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.IFluidPatch;

@Mixin(Fluid.class)
public abstract class FluidMixin implements IFluidPatch {

    @Override
    public double getFlowStrength(DimensionType type) {
        return 0.014;
    }
}
