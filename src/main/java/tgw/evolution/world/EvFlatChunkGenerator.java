package tgw.evolution.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;

public class EvFlatChunkGenerator extends ChunkGenerator<EvFlatChunkGenerator.Config> {

    private static final BlockState BEDROCK = Blocks.BEDROCK.getDefaultState();
    private static final BlockState STONE = Blocks.STONE.getDefaultState();

    public EvFlatChunkGenerator(IWorld worldIn, BiomeProvider biomeProviderIn) {
        super(worldIn, biomeProviderIn, Config.createDefault());
    }

    @Override
    public void generateSurface(IChunk chunkIn) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunkIn.setBlockState(mutablePos.setPos(x, 0, z), BEDROCK, false);
                for (int y = 1; y < 65; y++) {
                    mutablePos.setY(y);
                    chunkIn.setBlockState(mutablePos, STONE, false);
                }
            }
        }
    }

    @Override
    public int getGroundHeight() {
        return this.world.getSeaLevel() + 1;
    }

    @Override
    public void makeBase(IWorld worldIn, IChunk chunkIn) {

    }

    @Override
    public int func_222529_a(int p_222529_1_, int p_222529_2_, Heightmap.Type heightmapType) {
        return 0;
    }

    public static class Config extends GenerationSettings {

        public static Config createDefault() {
            return new Config();
        }
    }
}
