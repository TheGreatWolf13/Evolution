package tgw.evolution.init;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.world.biomes.EvolutionBiomeMaker;

public final class EvolutionBiomes {

    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, Evolution.MODID);

    public static final RegistryObject<Biome> FOREST = BIOMES.register("forest", EvolutionBiomeMaker::makeForestBiome);

    private EvolutionBiomes() {
    }

    public static void register() {
        BIOMES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private static void registerBiome(RegistryObject<Biome> biome, Type... types) {
        RegistryKey<Biome> key = RegistryKey.create(Registry.BIOME_REGISTRY, biome.getId());
        BiomeDictionary.addTypes(key, types);
        BiomeManager.addAdditionalOverworldBiomes(key);
    }

    public static void registerBiomes() {
        registerBiome(FOREST, Type.FOREST);
    }
}
