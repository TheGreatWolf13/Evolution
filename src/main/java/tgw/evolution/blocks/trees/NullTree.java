package tgw.evolution.blocks.trees;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.trees.Tree;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

public class NullTree extends Tree {

	@Override
	@Nullable
	protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random) {
		return null;
	}
}
