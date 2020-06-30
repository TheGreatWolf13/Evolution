package tgw.evolution.blocks.trees;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import tgw.evolution.world.feature.tree.BigTreeFeature;
import tgw.evolution.world.feature.tree.TreeFeature;

import javax.annotation.Nullable;
import java.util.Random;

public class OakTree extends Tree {

    @Nullable
    @Override
    protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random) {
        return random.nextInt(10) == 0 ? new BigTreeFeature(NoFeatureConfig::deserialize, true) : new TreeFeature(NoFeatureConfig::deserialize, true);
    }
}
