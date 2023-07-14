package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import tgw.evolution.util.collection.ICollectionExtension;

public interface O2SMap<K> extends Object2ShortMap<K>, ICollectionExtension {

    @Override
    void clear();
}
