package tgw.evolution.world.gen.carver;

import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;

public class EvolutionCarvers {

    public static final DeferredRegister<WorldCarver<?>> CARVERS = new DeferredRegister<>(ForgeRegistries.WORLD_CARVERS, Evolution.MODID);

    public static final RegistryObject<WorldCarver<ProbabilityConfig>> CAVE = CARVERS.register("cave", () -> new CaveWorldCarver(ProbabilityConfig::deserialize, 256));
    public static final RegistryObject<WorldCarver<ProbabilityConfig>> CANYON = CARVERS.register("canyon", () -> new CanyonWorldCarver(ProbabilityConfig::deserialize));

    public static void register() {
        CARVERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
