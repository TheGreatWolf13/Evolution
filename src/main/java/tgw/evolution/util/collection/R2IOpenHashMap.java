package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

public class R2IOpenHashMap<K> extends Reference2IntOpenHashMap<K> implements R2IMap<K> {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
