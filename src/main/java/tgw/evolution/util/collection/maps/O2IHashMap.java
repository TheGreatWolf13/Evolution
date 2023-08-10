package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import tgw.evolution.Evolution;

public class O2IHashMap<K> extends Object2IntOpenHashMap<K> implements O2IMap<K> {

    @Override
    public Integer get(Object key) {
        Evolution.deprecatedMethod();
        return super.get(key);
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
