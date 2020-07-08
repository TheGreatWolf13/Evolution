package tgw.evolution.world.feature;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import tgw.evolution.blocks.IStoneVariant;

import java.util.Random;
import java.util.function.Function;

public class FeatureRock extends Feature<NoFeatureConfig> {

    public FeatureRock(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactoryIn) {
        super(configFactoryIn);
    }

    @Override
    public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        int i = 0;
        for (int j = 0; j < 32; ++j) {
            BlockPos blockpos = pos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
            BlockState rockState = this.getRock(worldIn, blockpos);
            if (worldIn.isAirBlock(blockpos) && blockpos.getY() < 255 && rockState.isValidPosition(worldIn, blockpos)) {
                worldIn.setBlockState(blockpos, rockState, 2);
                ++i;
            }
        }
        return i > 0;
    }

    public BlockState getRock(IWorld world, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        for (int i = pos.getY(); i > 55; i--) {
            mutablePos.setY(i);
            Block block = world.getBlockState(mutablePos).getBlock();
            if (block instanceof IStoneVariant) {
                return ((IStoneVariant) block).getVariant().getRock().getDefaultState();
            }
        }
        return Blocks.AIR.getDefaultState();
    }
}
