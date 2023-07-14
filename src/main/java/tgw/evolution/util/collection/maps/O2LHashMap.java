package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import tgw.evolution.Evolution;

public class O2LHashMap<K> extends Object2LongOpenHashMap<K> implements O2LMap<K> {

    public O2LHashMap(int expected) {
        super(expected);
    }

    public O2LHashMap() {
    }

    @Override
    public FastEntrySet<K> object2LongEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set!");
        }
        return super.object2LongEntrySet();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
