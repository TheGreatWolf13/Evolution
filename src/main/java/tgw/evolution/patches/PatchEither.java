package tgw.evolution.patches;

import org.jetbrains.annotations.Nullable;

public interface PatchEither<L, R> {

    default L getLeft() {
        throw new AbstractMethodError();
    }

    default R getRight() {
        throw new AbstractMethodError();
    }

    default boolean isLeft() {
        throw new AbstractMethodError();
    }

    default boolean isRight() {
        throw new AbstractMethodError();
    }

    default @Nullable L leftOrNull() {
        throw new AbstractMethodError();
    }

    default @Nullable R rightOrNull() {
        throw new AbstractMethodError();
    }
}
