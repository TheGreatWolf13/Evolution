package tgw.evolution.blocks.trees;

import net.minecraft.block.trees.BigTree;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import tgw.evolution.blocks.BlockLog;
import tgw.evolution.blocks.BlockSapling;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.world.feature.tree.KapokBigTreeFeature;
import tgw.evolution.world.feature.tree.TreeFeature;

import javax.annotation.Nullable;
import java.util.Random;

public class KapokTree extends BigTree {

    @Nullable
    @Override
    protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random) {
        return new TreeFeature(NoFeatureConfig::deserialize, true, 4 + random.nextInt(7), EvolutionBlocks.LOG_KAPOK.get().getDefaultState().with(BlockLog.TREE, true), EvolutionBlocks.LEAVES_KAPOK.get().getDefaultState()).setSapling((BlockSapling) EvolutionBlocks.SAPLING_KAPOK.get());
    }

    @Nullable
    @Override
    protected AbstractTreeFeature<NoFeatureConfig> getBigTreeFeature(Random random) {
        return new KapokBigTreeFeature(NoFeatureConfig::deserialize, true, 10, 20, EvolutionBlocks.LOG_KAPOK.get().getDefaultState().with(BlockLog.TREE, true), EvolutionBlocks.LEAVES_KAPOK.get().getDefaultState()).setSapling((BlockSapling) EvolutionBlocks.SAPLING_KAPOK.get());
    }
}
