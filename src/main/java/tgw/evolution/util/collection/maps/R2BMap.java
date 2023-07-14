package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Reference2ByteMap;
import tgw.evolution.util.collection.ICollectionExtension;

public interface R2BMap<K> extends Reference2ByteMap<K>, ICollectionExtension {

    @Override
    void clear();
}
