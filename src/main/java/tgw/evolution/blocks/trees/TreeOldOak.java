//package tgw.evolution.blocks.trees;
//
//import net.minecraft.block.trees.BigTree;
//import net.minecraft.world.gen.feature.AbstractTreeFeature;
//import net.minecraft.world.gen.feature.ConfiguredFeature;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import tgw.evolution.world.feature.tree.OldOakBigTreeFeature;
//
//import javax.annotation.Nullable;
//import java.util.Random;
//
//public class TreeOldOak extends BigTree {
//    @Nullable
//    @Override
//    protected AbstractTreeFeature<NoFeatureConfig> getBigTreeFeature(Random random) {
//        return new OldOakBigTreeFeature(NoFeatureConfig::deserialize, true);
//    }
//
//    @Nullable
//    @Override
//    protected ConfiguredFeature<NoFeatureConfig> getTreeFeature(Random random) {
//        return null;
//    }
//}
