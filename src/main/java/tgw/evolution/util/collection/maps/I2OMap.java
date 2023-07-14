package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import tgw.evolution.util.collection.ICollectionExtension;

public interface I2OMap<V> extends Int2ObjectMap<V>, ICollectionExtension {

    @Override
    void clear();
}
