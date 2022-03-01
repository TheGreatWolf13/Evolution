//package tgw.evolution.world.biomes;
//
//import net.minecraft.entity.EntityClassification;
//import net.minecraft.util.math.MathHelper;
//import net.minecraft.world.biome.BiomeGenerationSettings;
//import net.minecraft.world.biome.MobSpawnInfo;
//import net.minecraft.world.gen.GenerationStage;
//import net.minecraft.world.gen.carver.ConfiguredCarvers;
//import net.minecraft.world.gen.feature.Features;
//import tgw.evolution.init.EvolutionEntities;
//
//public final class EvolutionBiomeFeatures {
//
//    private EvolutionBiomeFeatures() {
//    }
//
////    public static void addCaves(Biome biome) {
////        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
////                         Biome.createDecoratedFeature(EvolutionFeatures.STRUCTURE_CAVE.get(),
////                                                      IFeatureConfig.NO_FEATURE_CONFIG,
////                                                      Placement.NOPE,
////                                                      IPlacementConfig.NO_PLACEMENT_CONFIG));
////        biome.addStructure(EvolutionFeatures.STRUCTURE_CAVE.get(), IFeatureConfig.NO_FEATURE_CONFIG);
////    }
//
////    public static void addForestTrees(Biome biome) {
////        biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
////                         Biome.createDecoratedFeature(Feature.RANDOM_SELECTOR,
////                                                      new MultipleRandomFeatureConfig(new Feature[]{EvolutionFeatures.TREE_BIRCH.get(),
////                                                                                                    EvolutionFeatures.TREE_BIG_OAK.get()},
////                                                                                      new IFeatureConfig[]{IFeatureConfig.NO_FEATURE_CONFIG,
////                                                                                                           IFeatureConfig.NO_FEATURE_CONFIG},
////                                                                                      new float[]{0.2F, 0.1F},
////                                                                                      EvolutionFeatures.TREE_OAK.get(),
////                                                                                      IFeatureConfig.NO_FEATURE_CONFIG),
////                                                      Placement.COUNT_EXTRA_HEIGHTMAP,
////                                                      new AtSurfaceWithExtraConfig(10, 0.1F, 1)));
////    }
//
////    public static void addRocks(Biome biome) {
////        biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
////                         Biome.createDecoratedFeature(EvolutionFeatures.ROCK.get(),
////                                                      IFeatureConfig.NO_FEATURE_CONFIG,
////                                                      Placement.COUNT_HEIGHTMAP_32,
////                                                      new FrequencyConfig(16)));
////    }
//
////    public static void addSedimentatyDisks(Biome biome) {
////        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
////                         Biome.createDecoratedFeature(EvolutionFeatures.SEDIMENTARY_DISKS.get(),
////                                                      new SphereReplaceConfig(Blocks.SAND.getDefaultState(),
////                                                                              10,
////                                                                              2,
////                                                                              Lists.newArrayList(Blocks.DIRT.getDefaultState(),
////                                                                                                 Blocks.GRASS_BLOCK.getDefaultState())),
////                                                      Placement.COUNT_TOP_SOLID,
////                                                      new FrequencyConfig(3)));
////        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
////                         Biome.createDecoratedFeature(EvolutionFeatures.SEDIMENTARY_DISKS.get(),
////                                                      new SphereReplaceConfig(EvolutionBlocks.CLAY.get().getDefaultState(),
////                                                                              4,
////                                                                              1,
////                                                                              Lists.newArrayList(Blocks.DIRT.getDefaultState(),
////                                                                                                 EvolutionBlocks.CLAY.get().getDefaultState())),
////                                                      Placement.COUNT_TOP_SOLID,
////                                                      new FrequencyConfig(1)));
////        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
////                         Biome.createDecoratedFeature(EvolutionFeatures.SEDIMENTARY_DISKS.get(),
////                                                      new SphereReplaceConfig(Blocks.GRAVEL.getDefaultState(),
////                                                                              7,
////                                                                              2,
////                                                                              Lists.newArrayList(Blocks.DIRT.getDefaultState(),
////                                                                                                 Blocks.GRASS_BLOCK.getDefaultState())),
////                                                      Placement.COUNT_TOP_SOLID,
////                                                      new FrequencyConfig(1)));
////    }
//
////    public static void addStrata(Biome biome) {
////        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION,
////                         Biome.createDecoratedFeature(EvolutionFeatures.STRATA.get(),
////                                                      IFeatureConfig.NO_FEATURE_CONFIG,
////                                                      Placement.COUNT_RANGE,
////                                                      new CountRangeConfig(1, 0, 0, 256)));
////    }
//
////    public static void addTestStructure(Biome biome) {
////        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
////                         Biome.createDecoratedFeature(EvolutionFeatures.STRUCTURE_TEST.get(),
////                                                      IFeatureConfig.NO_FEATURE_CONFIG,
////                                                      Placement.NOPE,
////                                                      IPlacementConfig.NO_PLACEMENT_CONFIG));
////        biome.addStructure(EvolutionFeatures.STRUCTURE_TEST.get(), IFeatureConfig.NO_FEATURE_CONFIG);
////    }
//
////    public static void addVariantClusters(Biome biome) {
////        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
////                         Biome.createDecoratedFeature(Feature.ORE,
////                                                      new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE,
////                                                                           Blocks.DIRT.getDefaultState(),
////                                                                           33),
////                                                      Placement.COUNT_RANGE,
////                                                      new CountRangeConfig(10, 0, 0, 256)));
////        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
////                         Biome.createDecoratedFeature(Feature.ORE,
////                                                      new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE,
////                                                                           Blocks.GRAVEL.getDefaultState(),
////                                                                           33),
////                                                      Placement.COUNT_RANGE,
////                                                      new CountRangeConfig(8, 0, 0, 256)));
////    }
//
//    public static int getSkyColorWithTemperatureModifier(float temperature) {
//        float lvt_1_1_ = temperature / 3.0F;
//        lvt_1_1_ = MathHelper.clamp(lvt_1_1_, -1.0F, 1.0F);
//        return MathHelper.hsvToRgb(0.622_222_24F - lvt_1_1_ * 0.05F, 0.5F + lvt_1_1_ * 0.1F, 1.0F);
//    }
//
//    public static MobSpawnInfo.Builder getStandardMobSpawnBuilder() {
//        MobSpawnInfo.Builder spawns = new MobSpawnInfo.Builder();
//        withPassiveMobs(spawns);
//        return spawns;
//    }
//
//    public static void withCavesAndCanyons(BiomeGenerationSettings.Builder generator) {
//        generator.addCarver(GenerationStage.Carving.AIR, ConfiguredCarvers.CAVE);
//        generator.addCarver(GenerationStage.Carving.AIR, ConfiguredCarvers.CANYON);
//    }
//
//    public static void withLavaAndWaterLakes(BiomeGenerationSettings.Builder generator) {
//        generator.addFeature(GenerationStage.Decoration.LAKES, Features.LAKE_WATER);
//        generator.addFeature(GenerationStage.Decoration.LAKES, Features.LAKE_LAVA);
//    }
//
//    public static void withPassiveMobs(MobSpawnInfo.Builder spawns) {
//        spawns.addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EvolutionEntities.COW.get(), 8, 1, 3));
//    }
//}
