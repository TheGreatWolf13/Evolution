package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

public interface PatchLevel {

    void destroyBlockProgress(int breakerId, long pos, int progress);

    LevelChunk getChunkAt_(int x, int z);

    BlockPos getRandomPosInChunk(int x, int y, int z, int mask, BlockPos.MutableBlockPos out);
}
