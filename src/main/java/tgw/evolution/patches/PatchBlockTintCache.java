package tgw.evolution.patches;

import tgw.evolution.util.collection.III2IFunction;

public interface PatchBlockTintCache {

    default int getColor_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void setSource(III2IFunction source) {
        throw new AbstractMethodError();
    }
}
