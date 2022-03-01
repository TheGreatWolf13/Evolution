package tgw.evolution.patches;

import net.minecraft.core.BlockPos;

public interface ILevelPatch {

    void getRandomPosInChunk(int x, int y, int z, int mask, BlockPos.MutableBlockPos out);
}
