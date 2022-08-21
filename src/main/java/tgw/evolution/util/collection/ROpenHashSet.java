package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

public class ROpenHashSet<K> extends ReferenceOpenHashSet<K> implements RSet<K> {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
