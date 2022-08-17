package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class I2OOpenHashMap<V> extends Int2ObjectOpenHashMap<V> implements I2OMap<V> {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
