package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class O2IHashMap<K> extends Object2IntOpenHashMap<K> implements O2IMap<K> {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
