package tgw.evolution.world.biomes;

import net.minecraft.entity.EntityClassification;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import tgw.evolution.init.EvolutionEntities;

public class BiomeForest extends Biome implements IEvolutionBiome {

    public BiomeForest() {
        super(new Builder().surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG)
                           .precipitation(Biome.RainType.RAIN)
                           .category(Biome.Category.FOREST)
                           .depth(0.1F)
                           .scale(0.2F)
                           .temperature(0.7F)
                           .downfall(0.8F)
                           .waterColor(4159204)
                           .waterFogColor(329011)
                           .parent(null));
    }

    @Override
    public void init() {
        EvolutionBiomeFeatures.addVariantClusters(this);
        EvolutionBiomeFeatures.addSedimentatyDisks(this);
        EvolutionBiomeFeatures.addCaves(this);
        EvolutionBiomeFeatures.addStrata(this);
        EvolutionBiomeFeatures.addForestTrees(this);
        EvolutionBiomeFeatures.addRocks(this);
        this.addSpawn(EntityClassification.CREATURE, new SpawnListEntry(EvolutionEntities.COW.get(), 8, 1, 3));
//        this.addSpawn(EntityClassification.CREATURE, new SpawnListEntry(EvolutionEntities.BULL.get(), 8, 1, 3));
    }
}
