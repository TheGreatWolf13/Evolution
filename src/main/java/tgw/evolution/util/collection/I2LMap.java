package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.ints.Int2LongMap;

public interface I2LMap extends Int2LongMap, ICollectionExtension {

    @Override
    void clear();
}
