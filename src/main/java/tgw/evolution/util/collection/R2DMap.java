package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;

public interface R2DMap<K> extends Reference2DoubleMap<K>, ICollectionExtension {

    @Override
    void clear();
}
