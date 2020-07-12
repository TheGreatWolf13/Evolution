package tgw.evolution.init;

import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.fluids.FluidFreshWater;

public class EvolutionFluids {

    public static final DeferredRegister<Fluid> FLUIDS = new DeferredRegister<>(ForgeRegistries.FLUIDS, Evolution.MODID);
    public static final RegistryObject<FlowingFluid> FRESH_WATER = FLUIDS.register("fresh_water", FluidFreshWater.Source::new);
    public static final RegistryObject<FlowingFluid> FRESH_WATER_FLOWING = FLUIDS.register("fresh_water_flowing", FluidFreshWater.Flowing::new);

    public static void register() {
        FLUIDS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
