package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.Evolution;

import java.util.Collection;

public class OHashSet<K> extends ObjectOpenHashSet<K> implements OSet<K> {

    protected int lastPos = -1;
    private View view;

    public OHashSet(ObjectCollection<? extends K> c) {
        super(c);
    }

    public OHashSet(int expected) {
        super(expected);
    }

    public OHashSet(Collection<? extends K> c) {
        super(c);
    }

    public OHashSet() {
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
    public @Nullable K getElement() {
        if (this.isEmpty()) {
            return null;
        }
        for (int pos = this.n; pos-- != 0; ) {
            K k = this.key[pos];
            if (k != null) {
                return k;
            }
        }
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

    @Override
    public @UnmodifiableView OSet<K> view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
