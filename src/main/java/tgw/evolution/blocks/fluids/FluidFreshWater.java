package tgw.evolution.blocks.fluids;

import net.minecraft.fluid.IFluidState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidAttributes;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionFluids;

public abstract class FluidFreshWater extends FluidGeneric {

    public static final ResourceLocation FLUID_STILL = Evolution.location("block/fluid/fresh_water");
    public static final ITextComponent COMP = new TranslationTextComponent("evolution.fluid.fresh_water");

    protected FluidFreshWater(Properties properties) {
        super(properties);
    }

    private static Properties makeProperties() {
        return new Properties(EvolutionFluids.FRESH_WATER,
                              FluidAttributes.builder(FLUID_STILL, FLUID_STILL).color(0x4003_0ffc)).block(EvolutionBlocks.FRESH_WATER);
    }

    @Override
    public int getId() {
        return FRESH_WATER;
    }

    @Override
    public int getLevel(IFluidState state) {
        return state.get(LEVEL_1_8);
    }

    @Override
    public ITextComponent getTextComp() {
        return COMP;
    }

    public static class Flowing extends FluidFreshWater {

        public Flowing() {
            super(FluidFreshWater.makeProperties());
        }

        @Override
        public boolean isSource(IFluidState state) {
            return false;
        }
    }

    public static class Source extends FluidFreshWater {

        public Source() {
            super(FluidFreshWater.makeProperties());
        }

        @Override
        public boolean isSource(IFluidState state) {
            return true;
        }
    }
}
