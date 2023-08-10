package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import tgw.evolution.Evolution;

public class I2IHashMap extends Int2IntOpenHashMap implements I2IMap {

    @Override
    public Integer get(Object key) {
        Evolution.deprecatedMethod();
        return super.get(key);
    }

    @Override
    public FastEntrySet int2IntEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set!");
        }
        return super.int2IntEntrySet();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
