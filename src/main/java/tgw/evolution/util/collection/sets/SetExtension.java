package tgw.evolution.util.collection.sets;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import tgw.evolution.Evolution;

public interface SetExtension {

    boolean CHECKS = false;

    void clear();

    default void deprecatedMethod() {
        if (CHECKS) {
            Evolution.deprecatedMethod();
        }
    }

    default boolean hasNextIteration(long it) {
        return (int) it != 0;
    }

    default void reset() {
        this.clear();
        this.trim();
    }

    @CanIgnoreReturnValue
    boolean trim();
}
