package tgw.evolution.mixin;

import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.WaterFluid;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.IFluidPatch;
import tgw.evolution.util.physics.Fluid;

@Mixin(WaterFluid.class)
public abstract class WaterFluidMixin extends FlowingFluid implements IFluidPatch {

    @Override
    public Fluid fluid() {
        return Fluid.WATER;
    }

    @Override
    public double getFlowStrength(DimensionType type) {
        return 0.014;
    }
}
