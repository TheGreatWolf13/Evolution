package tgw.evolution.blocks.trees;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.world.feature.tree.AspenTreeFeature;

import javax.annotation.Nullable;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.TREE;

public class BirchTree extends Tree {

    @Override
    @Nullable
    protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random) {
        return new AspenTreeFeature(NoFeatureConfig::deserialize,
                                    false,
                                    EvolutionBlocks.LOG_BIRCH.get().getDefaultState().with(TREE, true),
                                    EvolutionBlocks.LEAVES_BIRCH.get().getDefaultState(),
                                    1 + random.nextInt(3));
    }
}
