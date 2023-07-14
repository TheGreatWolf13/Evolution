package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

public class RHashSet<K> extends ReferenceOpenHashSet<K> implements RSet<K> {

    protected int lastPos = -1;

    public RHashSet(ReferenceCollection<? extends K> c) {
        super(c);
    }

    public RHashSet() {
        super();
    }

    @Override
    public @Nullable K fastEntries() {
        if (this.isEmpty()) {
            return null;
        }
        if (this.lastPos == -1) {
            //Begin iteration
            this.lastPos = this.n;
        }
        for (int pos = this.lastPos; pos-- != 0; ) {
            K k = this.key[pos];
            if (k != null) {
                //Remember last pos
                this.lastPos = pos;
                return k;
            }
        }
        this.lastPos = -1;
        return null;
    }

    @Override
    public ObjectIterator<K> iterator() {
        if (CHECKS) {
            Evolution.info("Allocating memory for an iterator!");
        }
        return super.iterator();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
