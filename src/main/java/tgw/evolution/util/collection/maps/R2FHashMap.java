package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import tgw.evolution.Evolution;

public class R2FHashMap<K> extends Reference2FloatOpenHashMap<K> implements R2FMap<K> {

    @Override
    public Float get(Object key) {
        Evolution.deprecatedMethod();
        return super.get(key);
    }

    @Override
    public FastEntrySet<K> reference2FloatEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set");
        }
        return super.reference2FloatEntrySet();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
