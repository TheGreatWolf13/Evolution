package tgw.evolution.patches;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface PatchChunkStorage {

    default @Nullable CompoundTag read_(long chunkPos) throws IOException {
        throw new AbstractMethodError();
    }

    default void write_(long chunkPos, CompoundTag compoundTag) {
        throw new AbstractMethodError();
    }
}
