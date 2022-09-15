//package tgw.evolution.blocks.trees;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.Blocks;
//import net.minecraft.block.trees.BigTree;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.gen.feature.AbstractTreeFeature;
//import net.minecraft.world.gen.feature.IFeatureConfig;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//
//import org.jetbrains.annotations.Nullable;
//import java.util.Random;
//
//public abstract class TreeGenericGiant extends BigTree {
//
//    public static boolean canGiantTreeSpawnAt(BlockState blockUnder, IBlockReader world, BlockPos pos, int xOffset, int zOffset) {
//        Block block = blockUnder.getBlock();
//        return block == world.getBlockState(pos.add(xOffset, 0, zOffset)).getBlock() &&
//               block == world.getBlockState(pos.add(xOffset + 1, 0, zOffset)).getBlock() &&
//               block == world.getBlockState(pos.add(xOffset + 2, 0, zOffset)).getBlock() &&
//               block == world.getBlockState(pos.add(xOffset, 0, zOffset + 1)).getBlock() &&
//               block == world.getBlockState(pos.add(xOffset, 0, zOffset + 2)).getBlock() &&
//               block == world.getBlockState(pos.add(xOffset + 1, 0, zOffset + 1)).getBlock() &&
//               block == world.getBlockState(pos.add(xOffset + 2, 0, zOffset + 1)).getBlock() &&
//               block == world.getBlockState(pos.add(xOffset + 1, 0, zOffset + 2)).getBlock() &&
//               block == world.getBlockState(pos.add(xOffset + 2, 0, zOffset + 2)).getBlock();
//    }
//
//    @Nullable
//    protected abstract AbstractTreeFeature<NoFeatureConfig> getGiantTreeFeature(Random random);
//
//    @Override
//    public boolean spawn(IWorld worldIn, BlockPos pos, BlockState blockUnder, Random random) {
//        for (int i = 0; i >= -2; --i) {
//            for (int j = 0; j >= -2; --j) {
//                if (canGiantTreeSpawnAt(blockUnder, worldIn, pos, i, j)) {
//                    return this.spawnGiantTree(worldIn, pos, blockUnder, random, i, j);
//                }
//            }
//        }
//        return super.spawn(worldIn, pos, blockUnder, random);
//    }
//
//    public boolean spawnGiantTree(IWorld worldIn, BlockPos pos, BlockState blockUnder, Random random, int xOffset, int zOffset) {
//        AbstractTreeFeature<NoFeatureConfig> abstracttreefeature = this.getGiantTreeFeature(random);
//        if (abstracttreefeature == null) {
//            return false;
//        }
//        BlockState airState = Blocks.AIR.getDefaultState();
//        worldIn.setBlockState(pos.add(xOffset, 0, zOffset), airState, 4);
//        worldIn.setBlockState(pos.add(xOffset + 1, 0, zOffset), airState, 4);
//        worldIn.setBlockState(pos.add(xOffset + 2, 0, zOffset), airState, 4);
//        worldIn.setBlockState(pos.add(xOffset, 0, zOffset + 1), airState, 4);
//        worldIn.setBlockState(pos.add(xOffset, 0, zOffset + 2), airState, 4);
//        worldIn.setBlockState(pos.add(xOffset + 1, 0, zOffset + 1), airState, 4);
//        worldIn.setBlockState(pos.add(xOffset + 2, 0, zOffset + 1), airState, 4);
//        worldIn.setBlockState(pos.add(xOffset + 1, 0, zOffset + 2), airState, 4);
//        worldIn.setBlockState(pos.add(xOffset + 2, 0, zOffset + 2), airState, 4);
//        if (abstracttreefeature.place(worldIn,
//                                      worldIn.getChunkProvider().getChunkGenerator(),
//                                      random,
//                                      pos.add(xOffset, 0, zOffset),
//                                      IFeatureConfig.NO_FEATURE_CONFIG)) {
//            return true;
//        }
//        worldIn.setBlockState(pos.add(xOffset, 0, zOffset), blockUnder, 4);
//        worldIn.setBlockState(pos.add(xOffset + 1, 0, zOffset), blockUnder, 4);
//        worldIn.setBlockState(pos.add(xOffset + 2, 0, zOffset), blockUnder, 4);
//        worldIn.setBlockState(pos.add(xOffset, 0, zOffset + 1), blockUnder, 4);
//        worldIn.setBlockState(pos.add(xOffset, 0, zOffset + 2), blockUnder, 4);
//        worldIn.setBlockState(pos.add(xOffset + 1, 0, zOffset + 1), blockUnder, 4);
//        worldIn.setBlockState(pos.add(xOffset + 2, 0, zOffset + 1), blockUnder, 4);
//        worldIn.setBlockState(pos.add(xOffset + 1, 0, zOffset + 2), blockUnder, 4);
//        worldIn.setBlockState(pos.add(xOffset + 2, 0, zOffset + 2), blockUnder, 4);
//        return false;
//    }
//}