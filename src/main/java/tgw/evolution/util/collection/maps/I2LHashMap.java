package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import tgw.evolution.Evolution;

public class I2LHashMap extends Int2LongOpenHashMap implements I2LMap {

    @Override
    public FastEntrySet int2LongEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set!");
        }
        return super.int2LongEntrySet();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
