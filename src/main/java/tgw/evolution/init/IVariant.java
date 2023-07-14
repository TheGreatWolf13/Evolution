package tgw.evolution.init;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public interface IVariant {

    @Contract(pure = true, value = "null -> fail")
    default void checkNull(@Nullable Object o) {
        if (o == null) {
            throw new IllegalStateException("This variant (" + this + ") does not have a registry type for this registry!");
        }
    }

    String getName();
}
