package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import tgw.evolution.util.collection.ICollectionExtension;

public interface R2FMap<K> extends Reference2FloatMap<K>, ICollectionExtension {

    @Override
    void clear();
}
