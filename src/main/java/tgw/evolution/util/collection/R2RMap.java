package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;

public interface R2RMap<K, V> extends Reference2ReferenceMap<K, V>, ICollectionExtension {

    @Override
    void clear();
}
