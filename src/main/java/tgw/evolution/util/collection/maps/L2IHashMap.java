package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

public class L2IHashMap extends Long2IntOpenHashMap implements L2IMap {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
