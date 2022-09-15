package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;

public interface R2OMap<K, V> extends Reference2ObjectMap<K, V>, ICollectionExtension {

    @Override
    void clear();
}
