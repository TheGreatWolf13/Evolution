package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.Reference2FloatMap;

public interface R2FMap<K> extends Reference2FloatMap<K>, ICollectionExtension {

    @Override
    void clear();
}
