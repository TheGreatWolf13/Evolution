package tgw.evolution.mixin;

import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchFluid;

@Mixin(Fluid.class)
public abstract class MixinFluid implements PatchFluid {

    @Override
    public double getFlowStrength(DimensionType type) {
        return 0.014;
    }
}
