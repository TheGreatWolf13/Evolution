package tgw.evolution.patches;

import tgw.evolution.patches.replace.ChunkEntities;

import java.util.concurrent.CompletableFuture;

public interface PatchEntityStorage<T> {

    default CompletableFuture<ChunkEntities<T>> loadEntities_(long chunkPos) {
        throw new AbstractMethodError();
    }

    default void storeEntities_(ChunkEntities<T> chunkEntities) {
        throw new AbstractMethodError();
    }
}
