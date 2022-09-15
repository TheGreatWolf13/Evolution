package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;

public interface O2RMap<K, V> extends Object2ReferenceMap<K, V>, ICollectionExtension {

    @Override
    void clear();
}
