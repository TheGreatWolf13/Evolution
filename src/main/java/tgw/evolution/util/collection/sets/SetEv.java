package tgw.evolution.util.collection.sets;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import tgw.evolution.Evolution;

public interface SetEv {

    boolean CHECKS = true;
    long ITERATION_END = 0xFFFF_FFFFL;

    void clear();

    default void deprecatedMethod() {
        if (CHECKS) {
            Evolution.deprecatedMethod();
        }
    }

    default boolean hasNextIteration(long it) {
        return (it & ITERATION_END) != 0;
    }

    default void reset() {
        this.clear();
        this.trim();
    }

    @CanIgnoreReturnValue
    boolean trim();
}
