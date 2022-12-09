package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;

public class I2LOpenHashMap extends Int2LongOpenHashMap implements I2LMap {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
