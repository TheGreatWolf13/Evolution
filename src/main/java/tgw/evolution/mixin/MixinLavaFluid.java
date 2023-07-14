package tgw.evolution.mixin;

import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.util.physics.Fluid;

@Mixin(LavaFluid.class)
public abstract class MixinLavaFluid extends FlowingFluid {

    @Override
    public Fluid fluid() {
        return Fluid.LAVA;
    }

    @Override
    public double getFlowStrength(DimensionType type) {
        return type.ultraWarm() ? 0.007 : 0.002_333_333_333_333_333_5;
    }
}
