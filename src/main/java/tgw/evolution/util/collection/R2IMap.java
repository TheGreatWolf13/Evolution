package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;

public interface R2IMap<K> extends Reference2IntMap<K>, ICollectionExtension {

    @Override
    void clear();
}
