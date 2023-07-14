package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import tgw.evolution.Evolution;

public class O2SHashMap<K> extends Object2ShortOpenHashMap<K> implements O2SMap<K> {

    @Override
    public FastEntrySet<K> object2ShortEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set!");
        }
        return super.object2ShortEntrySet();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
