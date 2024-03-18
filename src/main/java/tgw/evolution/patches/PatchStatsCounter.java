package tgw.evolution.patches;

import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import tgw.evolution.util.collection.maps.O2LMap;

public interface PatchStatsCounter {

    static long properAdd(long a, long b) {
        assert a >= 0;
        assert b >= 0;
        long sum = a + b;
        if (sum < 0) {
            return Long.MAX_VALUE;
        }
        return sum;
    }

    /**
     * FOR INTERNAL USE ONLY. DO NOT CALL EXTERNALLY!
     */
    default O2LMap<Stat<?>> _getMap() {
        throw new AbstractMethodError();
    }

    default <T> long getValue_(StatType<T> statType, T stat) {
        throw new AbstractMethodError();
    }

    default long getValue_(Stat<?> stat) {
        throw new AbstractMethodError();
    }

    default void increment(Stat<?> stat, long amount) {
        throw new AbstractMethodError();
    }

    default void incrementPartial(Stat<?> stat, float amount) {
        throw new AbstractMethodError();
    }

    default void setValue_(Stat<?> stat, long amount) {
        throw new AbstractMethodError();
    }
}
