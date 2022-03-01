//package tgw.evolution.blocks.trees;
//
//import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
//import net.minecraft.world.gen.feature.ConfiguredFeature;
//import net.minecraft.world.level.block.grower.AbstractTreeGrower;
//import tgw.evolution.init.EvolutionConfiguredFeatures;
//
//import javax.annotation.Nullable;
//import java.util.Random;
//
//public class TreeOak extends AbstractTreeGrower {
//
//    @Nullable
//    @Override
//    protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredFeature(Random random, boolean largeHive) {
//        return random.nextInt(10) == 0 ? null : EvolutionConfiguredFeatures.TREE_OAK;
//    }
//}
