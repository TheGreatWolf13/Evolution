package tgw.evolution.patches;

import org.jetbrains.annotations.Nullable;

public interface PatchSectionStorage<R> {

    default void flush_(long chunkPos) {
        throw new AbstractMethodError();
    }

    default @Nullable R getOrLoad_(long secPos) {
        throw new AbstractMethodError();
    }

    default @Nullable R get_(long secPos) {
        throw new AbstractMethodError();
    }
}
