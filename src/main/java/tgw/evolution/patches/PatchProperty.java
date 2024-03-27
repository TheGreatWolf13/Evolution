package tgw.evolution.patches;

import org.jetbrains.annotations.Nullable;

public interface PatchProperty<T extends Comparable<T>> {

    default @Nullable T getValue_(String name) {
        throw new AbstractMethodError();
    }
}
