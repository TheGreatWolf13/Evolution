package tgw.evolution.init;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.fluids.FluidFreshWater;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.fluids.FluidSaltWater;

public final class EvolutionFluids {

    public static final RegistryObject<FluidGeneric> FRESH_WATER;
    public static final RegistryObject<FluidGeneric> SALT_WATER;
    //
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, Evolution.MODID);

    static {
        FRESH_WATER = FLUIDS.register("fresh_water", FluidFreshWater.Source::new);
        SALT_WATER = FLUIDS.register("salt_water", FluidSaltWater.Source::new);
    }

    private EvolutionFluids() {
    }

    public static void register() {
        FLUIDS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
