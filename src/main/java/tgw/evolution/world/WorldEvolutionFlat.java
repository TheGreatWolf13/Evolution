package tgw.evolution.world;

import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraftforge.common.world.ForgeWorldType;
import tgw.evolution.init.EvolutionBiomes;

import java.util.Optional;

public class WorldEvolutionFlat extends ForgeWorldType {

    public WorldEvolutionFlat() {
        super(null);
    }

    @Override
    public ChunkGenerator createChunkGenerator(Registry<Biome> biomeRegistry,
                                               Registry<DimensionSettings> dimensionSettingsRegistry,
                                               long seed,
                                               String generatorSettings) {
        FlatGenerationSettings settings = new FlatGenerationSettings(biomeRegistry, null, null, false, false, Optional.of(EvolutionBiomes.FOREST));
        return new ChunkGeneratorFlat(settings);
    }
}
