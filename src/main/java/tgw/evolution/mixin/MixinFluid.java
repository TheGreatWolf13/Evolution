package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchFluid;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(Fluid.class)
public abstract class MixinFluid implements PatchFluid {

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void animateTick(Level level, BlockPos pos, FluidState state, Random random) {
        Evolution.deprecatedMethod();
        this.animateTick_(level, pos.getX(), pos.getY(), pos.getZ(), state, random);
    }

    @Override
    public void animateTick_(Level level, int x, int y, int z, FluidState state, RandomGenerator random) {
    }

    @Override
    public double getFlowStrength(DimensionType type) {
        return 0.014;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    public boolean is(TagKey<Fluid> tag) {
        return this.fluid().tag() == tag;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void randomTick(Level level, BlockPos pos, FluidState fluidState, Random random) {
        Evolution.deprecatedMethod();
        this.randomTick_(level, pos.getX(), pos.getY(), pos.getZ(), fluidState, random);
    }

    @Override
    public void randomTick_(Level level, int x, int y, int z, FluidState fluidState, Random random) {
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void tick(Level level, BlockPos pos, FluidState state) {
        Evolution.deprecatedMethod();
        this.tick_(level, pos.getX(), pos.getY(), pos.getZ(), state);
    }

    @Override
    public void tick_(Level level, int x, int y, int z, FluidState state) {
    }
}
