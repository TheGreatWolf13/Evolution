package tgw.evolution.blocks.trees;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import tgw.evolution.blocks.BlockLog;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.world.feature.tree.AspenTreeFeature;

import javax.annotation.Nullable;
import java.util.Random;

public class BirchTree extends Tree {

    @Override
    @Nullable
    protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random) {
        return new AspenTreeFeature(NoFeatureConfig::deserialize, false, EvolutionBlocks.LOG_BIRCH.get().getDefaultState().with(BlockLog.TREE, true), EvolutionBlocks.LEAVES_BIRCH.get().getDefaultState(), 1 + random.nextInt(3));
    }
}
