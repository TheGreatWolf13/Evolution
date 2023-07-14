package tgw.evolution.init;

import net.minecraft.core.Registry;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.fluids.FluidFreshWater;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.fluids.FluidSaltWater;

public final class EvolutionFluids {

    public static final FluidGeneric FRESH_WATER;
    public static final FluidGeneric SALT_WATER;

    static {
        FRESH_WATER = register("fresh_water", new FluidFreshWater.Source());
        SALT_WATER = register("salt_water", new FluidSaltWater.Source());
    }

    private EvolutionFluids() {
    }

    private static <F extends FluidGeneric> F register(String name, F fluid) {
        return Registry.register(Registry.FLUID, Evolution.getResource(name), fluid);
    }

    public static void register() {
        //TODO implementation
    }
}
