package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class I2IOpenHashMap extends Int2IntOpenHashMap implements I2IMap {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
