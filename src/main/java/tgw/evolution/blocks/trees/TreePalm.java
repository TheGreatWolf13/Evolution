//package tgw.evolution.blocks.trees;
//
//import net.minecraft.block.trees.Tree;
//import net.minecraft.world.gen.feature.AbstractTreeFeature;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import tgw.evolution.world.feature.tree.PalmTreeFeature;
//
//import org.jetbrains.annotations.Nullable;
//import java.util.Random;
//
//public class TreePalm extends Tree {
//    @Nullable
//    @Override
//    protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random) {
//        return new PalmTreeFeature(NoFeatureConfig::deserialize, true);
//    }
//}
