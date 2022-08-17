package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class OArrayList<K> extends ObjectArrayList<K> implements OList<K> {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
