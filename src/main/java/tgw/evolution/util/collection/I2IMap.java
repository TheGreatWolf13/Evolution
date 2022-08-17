package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

public interface I2IMap extends Int2IntMap, ICollectionExtension {

    @Override
    void clear();
}
