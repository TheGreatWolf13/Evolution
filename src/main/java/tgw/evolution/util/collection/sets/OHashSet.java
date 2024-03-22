package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.Evolution;
import tgw.evolution.util.collection.lists.OList;

import java.util.Collection;

public class OHashSet<K> extends ObjectOpenHashSet<K> implements OSet<K> {

    protected int lastPos = -1;
    protected @Nullable View view;

    public OHashSet(ObjectCollection<? extends K> c) {
        super(c);
    }

    public OHashSet(int expected, float f) {
        super(expected, f);
    }

    public OHashSet(OList<? extends K> c) {
        this(c.size(), 0.75f);
        this.addAll(c);
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
    public boolean addAll(OList<? extends K> list) {
        this.preSize(list.size());
        return OSet.super.addAll(list);
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        if (c instanceof OList<? extends K> list) {
            return this.addAll(list);
        }
        return super.addAll(c);
    }

    private void ensureCapacity(int capacity) {
        int needed = HashCommon.arraySize(capacity, this.f);
        if (needed > this.n) {
            this.rehash(needed);
        }
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

    private void preSize(int size) {
        if (this.f <= 0.5) {
            this.ensureCapacity(size);
        }
        else {
            this.tryCapacity(this.size() + size);
        }
    }

    @Override
    public void trimCollection() {
        this.trim();
    }

    private void tryCapacity(long capacity) {
        int needed = (int) Math.min(0x4000_0000L, Math.max(2L, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity / this.f))));
        if (needed > this.n) {
            this.rehash(needed);
        }
    }

    @Override
    public @UnmodifiableView OSet<K> view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
