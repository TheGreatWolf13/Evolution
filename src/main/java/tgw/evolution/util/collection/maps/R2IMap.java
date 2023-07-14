package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import tgw.evolution.util.collection.ICollectionExtension;

public interface R2IMap<K> extends Reference2IntMap<K>, ICollectionExtension {

    @Override
    void clear();
}
