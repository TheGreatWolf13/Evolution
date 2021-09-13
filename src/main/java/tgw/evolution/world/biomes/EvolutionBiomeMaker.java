package tgw.evolution.world.biomes;

import net.minecraft.world.biome.*;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilders;

public class EvolutionBiomeMaker {

    public static Biome makeForestBiome() {
        MobSpawnInfo.Builder spawns = EvolutionBiomeFeatures.getStandardMobSpawnBuilder().setPlayerCanSpawn();
        return makeGenericForestBiome(0.1f, 0.2f, false, spawns);
    }

    private static Biome makeGenericForestBiome(float depth, float scale, boolean flowers, MobSpawnInfo.Builder spawns) {
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder().surfaceBuilder(ConfiguredSurfaceBuilders.GRASS);
        //TODO add caves
//        generation.withStructure(StructureFeatures.RUINED_PORTAL);
//        DefaultBiomeFeatures.withCavesAndCanyons(generation);
//        DefaultBiomeFeatures.withLavaAndWaterLakes(generation);
//        DefaultBiomeFeatures.withMonsterRoom(generation);
        if (flowers) {
//            generation.withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION_COMMON);
        }
        else {
//            DefaultBiomeFeatures.withAllForestFlowerGeneration(generation);
        }
//        DefaultBiomeFeatures.withCommonOverworldBlocks(generation);
//        DefaultBiomeFeatures.withOverworldOres(generation);
//        DefaultBiomeFeatures.withDisks(generation);
        if (flowers) {
//            generation.withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_TREES);
//            generation.withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.FLOWER_FOREST);
//            DefaultBiomeFeatures.withBadlandsGrass(generation);
        }
        else {
//            DefaultBiomeFeatures.withForestBirchTrees(generation);
//            DefaultBiomeFeatures.withDefaultFlowers(generation);
//            DefaultBiomeFeatures.withForestGrass(generation);
        }
//        DefaultBiomeFeatures.withNormalMushroomGeneration(generation);
//        DefaultBiomeFeatures.withSugarCaneAndPumpkins(generation);
//        DefaultBiomeFeatures.withLavaAndWaterSprings(generation);
//        DefaultBiomeFeatures.withFrozenTopLayer(generation);
        return new Biome.Builder().precipitation(Biome.RainType.RAIN)
                                  .biomeCategory(Biome.Category.FOREST)
                                  .depth(depth)
                                  .scale(scale)
                                  .temperature(0.7F)
                                  .downfall(0.8F)
                                  .specialEffects(new BiomeAmbience.Builder().waterColor(4_159_204)
                                                                             .waterFogColor(329_011)
                                                                             .fogColor(12_638_463)
                                                                             .skyColor(EvolutionBiomeFeatures.getSkyColorWithTemperatureModifier(0.7F))
                                                                             .ambientMoodSound(MoodSoundAmbience.LEGACY_CAVE_SETTINGS)
                                                                             .build())
                                  .mobSpawnSettings(spawns.build())
                                  .generationSettings(generation.build())
                                  .build();
    }

    public void init() {
//        EvolutionBiomeFeatures.addVariantClusters(this);
//        EvolutionBiomeFeatures.addSedimentatyDisks(this);
//        EvolutionBiomeFeatures.addCaves(this);
//        EvolutionBiomeFeatures.addStrata(this);
//        EvolutionBiomeFeatures.addForestTrees(this);
//        EvolutionBiomeFeatures.addRocks(this);
    }
}
