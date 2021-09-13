//package tgw.evolution.blocks.trees;
//
//import net.minecraft.block.trees.BigTree;
//import net.minecraft.world.gen.feature.AbstractTreeFeature;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import tgw.evolution.world.feature.tree.SpruceBigTreeFeature;
//import tgw.evolution.world.feature.tree.SpruceTreeFeature;
//
//import javax.annotation.Nullable;
//import java.util.Random;
//
//public class TreeSpruce extends BigTree {
//    @Nullable
//    @Override
//    protected AbstractTreeFeature<NoFeatureConfig> getBigTreeFeature(Random random) {
//        return new SpruceBigTreeFeature(NoFeatureConfig::deserialize, false, random.nextBoolean());
//    }
//
//    @Nullable
//    @Override
//    protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random) {
//        return new SpruceTreeFeature(NoFeatureConfig::deserialize, true);
//    }
//}
