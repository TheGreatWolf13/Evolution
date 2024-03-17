package tgw.evolution.util;

import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.math.ChunkPosMutable;

public class OptionalMutableChunkPos {

    private final ChunkPosMutable pos = new ChunkPosMutable();
    private boolean isPresent;

    public ChunkPos get() {
        if (!this.isPresent) {
            throw new NullPointerException("Optional is not present!");
        }
        return this.pos;
    }

    public @Nullable ChunkPos getOrNull() {
        if (!this.isPresent) {
            return null;
        }
        return this.pos;
    }

    public boolean isPresent() {
        return this.isPresent;
    }

    public void remove() {
        this.isPresent = false;
    }

    public void set(int x, int z) {
        this.isPresent = true;
        this.pos.set(x, z);
    }
}
