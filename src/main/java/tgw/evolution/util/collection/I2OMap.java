package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public interface I2OMap<V> extends Int2ObjectMap<V>, ICollectionExtension {

    @Override
    void clear();
}
