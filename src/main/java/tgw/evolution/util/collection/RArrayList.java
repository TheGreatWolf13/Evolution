package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

public class RArrayList<K> extends ReferenceArrayList<K> implements RList<K> {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
