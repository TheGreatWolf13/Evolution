package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Reference2ByteOpenHashMap;
import tgw.evolution.Evolution;

public class R2BHashMap<K> extends Reference2ByteOpenHashMap<K> implements R2BMap<K> {

    @Override
    public Byte get(Object key) {
        Evolution.deprecatedMethod();
        return super.get(key);
    }

    @Override
    public FastEntrySet<K> reference2ByteEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set!");
        }
        return super.reference2ByteEntrySet();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
