package tgw.evolution.world.biomes;

import com.google.common.collect.Lists;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.*;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.world.feature.EvolutionFeatures;
import tgw.evolution.world.gen.carver.EvolutionCarvers;

public class EvolutionBiomeFeatures {

    public static void addCarvers(Biome biome) {
        biome.addCarver(GenerationStage.Carving.AIR, Biome.createCarver(EvolutionCarvers.CAVE.get(), new ProbabilityConfig(0.14285715F)));
        biome.addCarver(GenerationStage.Carving.AIR, Biome.createCarver(EvolutionCarvers.CANYON.get(), new ProbabilityConfig(0.02F)));
    }

    public static void addTestStructure(Biome biome) {
        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, Biome.createDecoratedFeature(EvolutionFeatures.STRUCTURE_TEST.get(), IFeatureConfig.NO_FEATURE_CONFIG, Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));
        biome.addStructure(EvolutionFeatures.STRUCTURE_TEST.get(), IFeatureConfig.NO_FEATURE_CONFIG);
    }

    public static void addCaves(Biome biome) {
        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, Biome.createDecoratedFeature(EvolutionFeatures.STRUCTURE_CAVE.get(), IFeatureConfig.NO_FEATURE_CONFIG, Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));
        biome.addStructure(EvolutionFeatures.STRUCTURE_CAVE.get(), IFeatureConfig.NO_FEATURE_CONFIG);
    }

    public static void addForestTrees(Biome biome) {
        biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.RANDOM_SELECTOR, new MultipleRandomFeatureConfig(new Feature[]{
                EvolutionFeatures.TREE_BIRCH.get(),
                EvolutionFeatures.TREE_BIG_OAK.get()}, new IFeatureConfig[]{IFeatureConfig.NO_FEATURE_CONFIG,
                                                                            IFeatureConfig.NO_FEATURE_CONFIG}, new float[]{0.2F,
                                                                                                                           0.1F}, EvolutionFeatures.TREE_OAK.get(), IFeatureConfig.NO_FEATURE_CONFIG), Placement.COUNT_EXTRA_HEIGHTMAP, new AtSurfaceWithExtraConfig(10, 0.1F, 1)));
    }

    public static void addRocks(Biome biome) {
        biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(EvolutionFeatures.ROCK.get(), IFeatureConfig.NO_FEATURE_CONFIG, Placement.COUNT_HEIGHTMAP_32, new FrequencyConfig(16)));
    }

    public static void addStrata(Biome biome) {
        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, Biome.createDecoratedFeature(EvolutionFeatures.STRATA.get(), IFeatureConfig.NO_FEATURE_CONFIG, Placement.COUNT_RANGE, new CountRangeConfig(1, 0, 0, 256)));
    }

    public static void addVariantClusters(Biome biome) {
        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Biome.createDecoratedFeature(Feature.ORE, new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, Blocks.DIRT.getDefaultState(), 33), Placement.COUNT_RANGE, new CountRangeConfig(10, 0, 0, 256)));
        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Biome.createDecoratedFeature(Feature.ORE, new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, Blocks.GRAVEL.getDefaultState(), 33), Placement.COUNT_RANGE, new CountRangeConfig(8, 0, 0, 256)));
    }

    public static void addSedimentatyDisks(Biome biome) {
        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Biome.createDecoratedFeature(EvolutionFeatures.SEDIMENTARY_DISKS.get(), new SphereReplaceConfig(Blocks.SAND.getDefaultState(), 10, 2, Lists.newArrayList(Blocks.DIRT.getDefaultState(), Blocks.GRASS_BLOCK.getDefaultState())), Placement.COUNT_TOP_SOLID, new FrequencyConfig(3)));
        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Biome.createDecoratedFeature(EvolutionFeatures.SEDIMENTARY_DISKS.get(), new SphereReplaceConfig(EvolutionBlocks.CLAY.get().getDefaultState(), 4, 1, Lists.newArrayList(Blocks.DIRT.getDefaultState(), EvolutionBlocks.CLAY.get().getDefaultState())), Placement.COUNT_TOP_SOLID, new FrequencyConfig(1)));
        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Biome.createDecoratedFeature(EvolutionFeatures.SEDIMENTARY_DISKS.get(), new SphereReplaceConfig(Blocks.GRAVEL.getDefaultState(), 7, 2, Lists.newArrayList(Blocks.DIRT.getDefaultState(), Blocks.GRASS_BLOCK.getDefaultState())), Placement.COUNT_TOP_SOLID, new FrequencyConfig(1)));
    }
}
