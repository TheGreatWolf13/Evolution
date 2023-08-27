package tgw.evolution.patches;

import tgw.evolution.util.collection.maps.I2IMap;

public interface PatchCraftingContainer {

    default void getRemoveCounter(I2IMap counter) {
        throw new AbstractMethodError();
    }

    default void put(int slot, int amount) {
        throw new AbstractMethodError();
    }

    default void reset() {
        throw new AbstractMethodError();
    }
}
