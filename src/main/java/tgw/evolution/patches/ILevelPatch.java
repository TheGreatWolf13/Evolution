package tgw.evolution.patches;

import net.minecraft.core.BlockPos;

public interface ILevelPatch {

    void destroyBlockProgress(int breakerId, long pos, int progress);

    BlockPos getRandomPosInChunk(int x, int y, int z, int mask, BlockPos.MutableBlockPos out);
}
