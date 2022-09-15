//package tgw.evolution.blocks.trees;
//
//import net.minecraft.block.trees.Tree;
//import net.minecraft.world.gen.feature.AbstractTreeFeature;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import tgw.evolution.world.feature.tree.ElmTreeFeature;
//
//import org.jetbrains.annotations.Nullable;
//import java.util.Random;
//
//public class TreeElm extends Tree {
//
//    @Override
//    @Nullable
//    protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random) {
//        return new ElmTreeFeature(NoFeatureConfig::deserialize, true);
//    }
//}
