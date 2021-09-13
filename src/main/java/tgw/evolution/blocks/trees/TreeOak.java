package tgw.evolution.blocks.trees;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import tgw.evolution.init.EvolutionConfiguredFeatures;

import javax.annotation.Nullable;
import java.util.Random;

public class TreeOak extends Tree {

    @Nullable
    @Override
    protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredFeature(Random random, boolean largeHive) {
        return random.nextInt(10) == 0 ? null : EvolutionConfiguredFeatures.TREE_OAK;
    }
}
