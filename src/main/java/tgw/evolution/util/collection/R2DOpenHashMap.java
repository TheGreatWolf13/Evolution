package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;

public class R2DOpenHashMap<K> extends Reference2DoubleOpenHashMap<K> implements R2DMap<K> {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
