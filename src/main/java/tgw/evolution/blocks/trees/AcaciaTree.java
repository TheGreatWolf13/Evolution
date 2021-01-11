package tgw.evolution.blocks.trees;

import net.minecraft.block.trees.BigTree;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import tgw.evolution.world.feature.tree.AcaciaBigTreeFeature;
import tgw.evolution.world.feature.tree.AcaciaTreeFeature;

import javax.annotation.Nullable;
import java.util.Random;

public class AcaciaTree extends BigTree {
    @Override
    @Nullable
    protected AbstractTreeFeature<NoFeatureConfig> getBigTreeFeature(Random random) {
        return new AcaciaBigTreeFeature(NoFeatureConfig::deserialize, true);
    }

    @Nullable
    @Override
    protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random) {
        return new AcaciaTreeFeature(NoFeatureConfig::deserialize, true);
    }
}