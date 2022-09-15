package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public class R2OOpenHashMap<K, V> extends Reference2ObjectOpenHashMap<K, V> implements R2OMap<K, V> {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
