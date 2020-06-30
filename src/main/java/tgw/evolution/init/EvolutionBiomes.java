package tgw.evolution.init;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.world.biomes.BiomeForest;
import tgw.evolution.world.biomes.IEvolutionBiome;

public class EvolutionBiomes {

    public static final DeferredRegister<Biome> BIOMES = new DeferredRegister<>(ForgeRegistries.BIOMES, Evolution.MODID);

    public static final RegistryObject<Biome> FOREST = BIOMES.register("forest", BiomeForest::new);

    public static void register() {
        BIOMES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static void registerBiomes() {
        registerBiome(FOREST, Type.FOREST);
    }

    private static void registerBiome(RegistryObject<Biome> biome, Type... types) {
        BiomeDictionary.addTypes(biome.get(), types);
        BiomeManager.addSpawnBiome(biome.get());
        ((IEvolutionBiome) biome.get()).init();
    }
}
