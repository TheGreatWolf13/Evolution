package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchFluid;

import java.util.Random;

@Mixin(Fluid.class)
public abstract class MixinFluid implements PatchFluid {

    @Override
    public double getFlowStrength(DimensionType type) {
        return 0.014;
    }

    @Overwrite
    public void randomTick(Level level, BlockPos pos, FluidState fluidState, Random random) {
        Evolution.deprecatedMethod();
        this.randomTick_(level, pos.getX(), pos.getY(), pos.getZ(), fluidState, random);
    }

    @Override
    public void randomTick_(Level level, int x, int y, int z, FluidState fluidState, Random random) {
    }
}
