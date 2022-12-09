package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.Reference2ByteOpenHashMap;

public class R2BOpenHashMap<K> extends Reference2ByteOpenHashMap<K> implements R2BMap<K> {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
