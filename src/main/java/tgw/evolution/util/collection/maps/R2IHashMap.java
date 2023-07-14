package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import tgw.evolution.Evolution;

public class R2IHashMap<K> extends Reference2IntOpenHashMap<K> implements R2IMap<K> {

    public R2IHashMap(int expected) {
        super(expected);
    }

    public R2IHashMap() {
    }

    @Override
    public FastEntrySet<K> reference2IntEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set");
        }
        return super.reference2IntEntrySet();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
