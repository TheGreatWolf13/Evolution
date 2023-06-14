package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

public class ROpenHashSet<K> extends ReferenceOpenHashSet<K> implements RSet<K> {

    public ROpenHashSet(ReferenceCollection<? extends K> c) {
        super(c);
    }

    public ROpenHashSet() {
        super();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
