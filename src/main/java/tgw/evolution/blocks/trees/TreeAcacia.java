//package tgw.evolution.blocks.trees;
//
//import net.minecraft.block.trees.BigTree;
//import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
//import net.minecraft.world.gen.feature.ConfiguredFeature;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import tgw.evolution.world.feature.tree.AcaciaBigTreeFeature;
//import tgw.evolution.world.feature.tree.AcaciaTreeFeature;
//
//import javax.annotation.Nullable;
//import java.util.Random;
//
//public class TreeAcacia extends BigTree {
//    @Override
//    @Nullable
//    protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getHugeTreeFeature(Random random) {
//        return new AcaciaBigTreeFeature(NoFeatureConfig::deserialize, true);
//    }
//
//    @Nullable
//    @Override
//    protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getTreeFeature(Random random, boolean notify) {
//        return new AcaciaTreeFeature(NoFeatureConfig::deserialize, true);
//    }
//}