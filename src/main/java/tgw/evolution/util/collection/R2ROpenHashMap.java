package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

public class R2ROpenHashMap<K, V> extends Reference2ReferenceOpenHashMap<K, V> implements R2RMap<K, V> {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
