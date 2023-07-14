package tgw.evolution.util.math;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import tgw.evolution.mixin.AccessorChunkPos;

@SuppressWarnings("EqualsAndHashcode")
public class ChunkPosMutable extends ChunkPos {

    public ChunkPosMutable() {
        this(0, 0);
    }

    public ChunkPosMutable(ChunkPos pos) {
        this(pos.x, pos.z);
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
        ((AccessorChunkPos) this).setX(x);
        ((AccessorChunkPos) this).setZ(z);
        return this;
    }
}
