package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import tgw.evolution.util.collection.ICollectionExtension;

public interface O2LMap<K> extends Object2LongMap<K>, ICollectionExtension {

    @Override
    void clear();
}
