package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import tgw.evolution.util.collection.ICollectionExtension;

public interface I2IMap extends Int2IntMap, ICollectionExtension {

    @Override
    void clear();
}
