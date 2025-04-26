package tgw.evolution.patches;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface PatchIOWorker {

    default CompletableFuture<CompoundTag> loadAsync_(long chunkPos) {
        throw new AbstractMethodError();
    }

    default @Nullable CompoundTag load_(long chunkPos) throws IOException {
        throw new AbstractMethodError();
    }

    default CompletableFuture<Void> scanChunk_(long chunkPos, StreamTagVisitor visitor) {
        throw new AbstractMethodError();
    }

    default CompletableFuture<Void> store_(long chunkPos, @Nullable CompoundTag compoundTag) {
        throw new AbstractMethodError();
    }
}
