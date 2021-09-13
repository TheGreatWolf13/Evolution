package tgw.evolution.init;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.foliageplacer.BlobFoliagePlacer;
import net.minecraft.world.gen.foliageplacer.FancyFoliagePlacer;
import net.minecraft.world.gen.trunkplacer.StraightTrunkPlacer;
import tgw.evolution.Evolution;

public final class EvolutionConfiguredFeatures {

    public static final ConfiguredFeature<BaseTreeFeatureConfig, ?> TREE_BIRCH;
    public static final ConfiguredFeature<BaseTreeFeatureConfig, ?> TREE_OAK;

    static {
        TREE_BIRCH = register("tree_birch",
                              Feature.TREE.configured(new BaseTreeFeatureConfig.Builder(new SimpleBlockStateProvider(EvolutionBlocks.LOG_BIRCH.get()
                                                                                                                                              .defaultBlockState()),
                                                                                        new SimpleBlockStateProvider(EvolutionBlocks.LEAVES_BIRCH.get()
                                                                                                                                                 .defaultBlockState()),
                                                                                        new FancyFoliagePlacer(FeatureSpread.fixed(2),
                                                                                                               FeatureSpread.fixed(4),
                                                                                                               3),
                                                                                        new StraightTrunkPlacer(5, 9, 0),
                                                                                        new TwoLayerFeature(1, 0, 1)).build()));
        TREE_OAK = register("tree_oak",
                            Feature.TREE.configured(new BaseTreeFeatureConfig.Builder(new SimpleBlockStateProvider(EvolutionBlocks.LOG_OAK.get()
                                                                                                                                          .defaultBlockState()),
                                                                                      new SimpleBlockStateProvider(EvolutionBlocks.LEAVES_OAK.get()
                                                                                                                                             .defaultBlockState()),
                                                                                      new BlobFoliagePlacer(FeatureSpread.fixed(2),
                                                                                                            FeatureSpread.fixed(0),
                                                                                                            3),
                                                                                      new StraightTrunkPlacer(4, 2, 0),
                                                                                      new TwoLayerFeature(1, 0, 1)).build()));
    }

    private EvolutionConfiguredFeatures() {
    }

    private static <FC extends IFeatureConfig> ConfiguredFeature<FC, ?> register(String key, ConfiguredFeature<FC, ?> feature) {
        return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, Evolution.getResource(key), feature);
    }
}
