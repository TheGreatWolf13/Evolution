package tgw.evolution.patches;

import tgw.evolution.util.collection.maps.I2IMap;

public interface PatchCraftingContainer {

    void getRemoveCounter(I2IMap counter);

    void put(int slot, int amount);

    void reset();
}
