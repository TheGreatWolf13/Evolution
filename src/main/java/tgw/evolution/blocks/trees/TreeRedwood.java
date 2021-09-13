//package tgw.evolution.blocks.trees;
//
//import net.minecraft.world.gen.feature.AbstractTreeFeature;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import tgw.evolution.world.feature.tree.RedwoodGiantTreeFeature;
//
//import javax.annotation.Nullable;
//import java.util.Random;
//
//public class TreeRedwood extends TreeGenericGiant {
//
//    @Override
//    @Nullable
//    protected AbstractTreeFeature<NoFeatureConfig> getBigTreeFeature(Random random) {
//        return null;
//    }
//
//    @Override
//    protected AbstractTreeFeature<NoFeatureConfig> getGiantTreeFeature(Random random) {
//        return new RedwoodGiantTreeFeature(NoFeatureConfig::deserialize, true);
//    }
//
//    @Override
//    @Nullable
//    protected AbstractTreeFeature<NoFeatureConfig> getTreeFeature(Random random) {
//        return null;
//    }
//}
