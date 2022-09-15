package tgw.evolution.util.math;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import tgw.evolution.patches.IChunkPosPatch;

@SuppressWarnings("EqualsAndHashcode")
public class ChunkPosMutable extends ChunkPos {

    public ChunkPosMutable() {
        this(0, 0);
    }

    public ChunkPosMutable(int x, int y) {
        super(x, y);
    }

    @Override
    public int hashCode() {
        throw new IllegalStateException("Cannot hash mutable object");
    }

    public ChunkPosMutable set(BlockPos pos) {
        return this.set(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    public ChunkPosMutable set(int x, int z) {
        ((IChunkPosPatch) this).setX(x);
        ((IChunkPosPatch) this).setZ(z);
        return this;
    }
}
