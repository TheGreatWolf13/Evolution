package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import tgw.evolution.util.collection.ICollectionExtension;

public interface L2IMap extends Long2IntMap, ICollectionExtension {

    @Override
    void clear();
}
