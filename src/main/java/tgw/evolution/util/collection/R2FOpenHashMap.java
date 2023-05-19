package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;

public class R2FOpenHashMap<K> extends Reference2FloatOpenHashMap<K> implements R2FMap<K> {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
