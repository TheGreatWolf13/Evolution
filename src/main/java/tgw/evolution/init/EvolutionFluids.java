package tgw.evolution.init;

import net.minecraft.fluid.Fluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.fluids.FluidFreshWater;
import tgw.evolution.blocks.fluids.FluidGeneric;

public final class EvolutionFluids {

    public static final DeferredRegister<Fluid> FLUIDS = new DeferredRegister<>(ForgeRegistries.FLUIDS, Evolution.MODID);
    public static final RegistryObject<FluidGeneric> FRESH_WATER = FLUIDS.register("fresh_water", FluidFreshWater.Source::new);

    private EvolutionFluids() {
    }

    public static void register() {
        FLUIDS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
