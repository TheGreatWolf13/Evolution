package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.Evolution;

import java.util.Collection;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;

public class RHashSet<K> extends ReferenceOpenHashSet<K> implements RSet<K> {

    /**
     * Bit 0: shouldRehash; <br>
     * Bit 1: askedToRehash; <br>
     */
    protected byte flags = 0b01;
    protected int lastPos = -1;
    protected @Nullable View<K> view;

    public RHashSet(ReferenceCollection<? extends K> c) {
        super(c);
    }

    public RHashSet() {
        super();
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        if (c instanceof RSet) {
            return this.addAll((RSet<? extends K>) c);
        }
        Evolution.warn("Default addAll");
        return super.addAll(c);
    }

    @Override
    public boolean addAll(RSet<? extends K> set) {
        if (this.f <= 0.5) {
            this.ensureCapacity(set.size());
        }
        else {
            this.tryCapacity(this.size() + set.size());
        }
        return RSet.super.addAll(set);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c instanceof RSet<?> set) {
            return this.containsAll(set);
        }
        Evolution.warn("Default containsAll");
        return super.containsAll(c);
    }

    private void ensureCapacity(final int capacity) {
        final int needed = arraySize(capacity, this.f);
        if (needed > this.n) {
            this.rehash(needed);
        }
    }

    @Override
    public @Nullable K fastEntries() {
        if (this.isEmpty()) {
            this.handleRehash();
            return null;
        }
        if (this.lastPos == -1) {
            //Begin iteration
            this.lastPos = this.n;
            this.flags &= ~1;
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
        this.handleRehash();
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

    protected void handleRehash() {
        byte oldFlags = this.flags;
        this.flags = 0b01;
        if ((oldFlags & 2) != 0) {
            this.trim();
        }
    }

    @Override
    public ObjectIterator<K> iterator() {
        if (CHECKS) {
            Evolution.info("Allocating memory for an iterator!");
        }
        return super.iterator();
    }

    @Override
    protected void rehash(int newN) {
        if ((this.flags & 1) != 0) {
            super.rehash(newN);
        }
        else {
            this.flags |= 2;
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (c instanceof RSet<?> set) {
            return this.removeAll(set);
        }
        Evolution.warn("Default removeAll");
        return super.removeAll(c);
    }

    @Override
    public void resetIteration() {
        this.flags = 0b01;
        this.lastPos = -1;
    }

    @Override
    public void trimCollection() {
        this.trim();
    }

    private void tryCapacity(final long capacity) {
        final int needed = (int) Math.min(1 << 30, Math.max(2, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity / this.f))));
        if (needed > this.n) {
            this.rehash(needed);
        }
    }

    @Override
    public @UnmodifiableView RSet<K> view() {
        if (this.view == null) {
            this.view = new View<>(this);
        }
        return this.view;
    }
}
