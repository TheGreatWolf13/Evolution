package tgw.evolution.patches;

import tgw.evolution.util.collection.I2IMap;

public interface ICraftingContainerPatch {

    void getRemoveCounter(I2IMap counter);

    void put(int slot, int amount);

    void reset();
}
