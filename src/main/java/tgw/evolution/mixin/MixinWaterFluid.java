package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.physics.Fluid;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(WaterFluid.class)
public abstract class MixinWaterFluid extends FlowingFluid {

    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(Level level, BlockPos blockPos, FluidState fluidState, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(Level level, int x, int y, int z, FluidState state, RandomGenerator random) {
        if (!state.isSource() && !state.getValue(FALLING)) {
            if (random.nextInt(64) == 0) {
                level.playLocalSound(x + 0.5, y + 0.5, z + 0.5, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false);
            }
        }
        else if (random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.UNDERWATER, x + random.nextDouble(), y + random.nextDouble(), z + random.nextDouble(), 0, 0, 0);
        }
    }

    @Override
    public Fluid fluid() {
        return Fluid.WATER;
    }

    @Override
    public double getFlowStrength(DimensionType type) {
        return 0.014;
    }
}
