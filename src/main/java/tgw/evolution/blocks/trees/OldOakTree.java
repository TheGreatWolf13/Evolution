package tgw.evolution.blocks.trees;

import net.minecraft.block.trees.BigTree;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import tgw.evolution.world.feature.tree.OldOakBigTreeFeature;

import javax.annotation.Nullable;
import java.util.Random;

public class OldOakTree extends BigTree {
    @Nullable
    @Override
    protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random) {
        return null;
    }

    @Nullable
    @Override
    protected AbstractTreeFeature<NoFeatureConfig> getBigTreeFeature(Random random) {
        return new OldOakBigTreeFeature(NoFeatureConfig::deserialize, true);
    }
}
