//package tgw.evolution.world.feature;
//
//import com.mojang.serialization.Codec;
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.Blocks;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.ISeedReader;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.gen.ChunkGenerator;
//import net.minecraft.world.gen.feature.Feature;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import tgw.evolution.blocks.IRockVariant;
//
//import java.util.Random;
//
//public class FeatureRock extends Feature<NoFeatureConfig> {
//
//    public FeatureRock(Codec<NoFeatureConfig> configFactory) {
//        super(configFactory);
//    }
//
//    public BlockState getRock(IWorld world, BlockPos pos) {
//        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
//        mutablePos.set(pos);
//        for (int i = pos.getY(); i > 55; i--) {
//            mutablePos.setY(i);
//            Block block = world.getBlockState(mutablePos).getBlock();
//            if (block instanceof IRockVariant) {
//                return ((IRockVariant) block).getVariant().getRock().defaultBlockState();
//            }
//        }
//        return Blocks.AIR.defaultBlockState();
//    }
//
//    @Override
//    public boolean place(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
//        int count = 0;
//        BlockPos.Mutable chosenPos = new BlockPos.Mutable();
//        for (int i = 0; i < 32; i++) {
//            chosenPos.setWithOffset(pos, rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
//            BlockState rockState = this.getRock(world, chosenPos);
//            if (world.isEmptyBlock(chosenPos) && chosenPos.getY() < 255 && rockState.canSurvive(world, chosenPos)) {
//                this.setBlock(world, chosenPos, rockState);
//                count++;
//            }
//        }
//        return count > 0;
//    }
//}
