package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import tgw.evolution.Evolution;

public class I2OHashMap<V> extends Int2ObjectOpenHashMap<V> implements I2OMap<V> {

    @Override
    public FastEntrySet<V> int2ObjectEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set!");
        }
        return super.int2ObjectEntrySet();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
