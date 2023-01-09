package tgw.evolution.capabilities.chunkstorage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.INBTSerializable;
import tgw.evolution.blocks.IAir;

public interface IChunkStorage extends INBTSerializable<CompoundTag> {

    default void scheduleAtmTick(LevelChunk chunk, int x, int y, int z, boolean forceUpdate) {
        this.scheduleAtmTick(chunk, IAir.packInternalPos(x & 15, y, z & 15, forceUpdate));
    }

    void scheduleAtmTick(LevelChunk chunk, int internalPos);

    void scheduleBlockTick(LevelChunk chunk, long pos);

    boolean setContinuousAtmDebug(LevelChunk chunk, boolean debug);

    void tick(LevelChunk chunk);
}