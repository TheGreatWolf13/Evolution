package tgw.evolution.blocks.trees;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import tgw.evolution.world.feature.tree.AspenTreeFeature;

import javax.annotation.Nullable;
import java.util.Random;

public class AspenTree extends Tree {
    @Override
    @Nullable
    protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random) {
        return new AspenTreeFeature(NoFeatureConfig::deserialize, true);
    }
}