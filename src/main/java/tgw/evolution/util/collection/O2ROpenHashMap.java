package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

public class O2ROpenHashMap<K, V> extends Object2ReferenceOpenHashMap<K, V> implements O2RMap<K, V> {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
