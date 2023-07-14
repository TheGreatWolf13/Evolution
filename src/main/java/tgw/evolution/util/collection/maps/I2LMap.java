package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.ints.Int2LongMap;
import tgw.evolution.util.collection.ICollectionExtension;

public interface I2LMap extends Int2LongMap, ICollectionExtension {

    @Override
    void clear();
}
