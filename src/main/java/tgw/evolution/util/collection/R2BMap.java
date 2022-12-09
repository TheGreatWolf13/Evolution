package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.Reference2ByteMap;

public interface R2BMap<K> extends Reference2ByteMap<K>, ICollectionExtension {

    @Override
    void clear();
}
