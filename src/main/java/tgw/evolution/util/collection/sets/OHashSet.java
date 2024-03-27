package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.lists.OArrayList;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class OHashSet<K> extends ObjectOpenHashSet<K> implements OSet<K> {

    protected @Nullable View<K> view;
    protected @Nullable OArrayList<K> wrappedEntries;

    public OHashSet(int expected, float f) {
        super(expected, f);
    }

    public OHashSet(int expected) {
        super(expected);
    }

    public OHashSet() {
    }

    public OHashSet(Collection<? extends K> c, float f) {
        super(c, f);
    }

    public OHashSet(Collection<? extends K> c) {
        super(c);
    }

    public OHashSet(ObjectCollection<? extends K> c, float f) {
        super(c, f);
    }

    public OHashSet(ObjectCollection<? extends K> c) {
        super(c);
    }

    public OHashSet(Iterator<? extends K> i, float f) {
        super(i, f);
    }

    public OHashSet(Iterator<? extends K> i) {
        super(i);
    }

    public OHashSet(K[] a, int offset, int length, float f) {
        super(a, offset, length, f);
    }

    public OHashSet(K[] a, int offset, int length) {
        super(a, offset, length);
    }

    public OHashSet(K[] a, float f) {
        super(a, f);
    }

    public OHashSet(K[] a) {
        super(a);
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        if (this.f <= 0.5) {
            this.ensureCapacity(c.size());
        }
        else {
            this.tryCapacity(this.size() + c.size());
        }
        return OSet.super.addAll(c);
    }

    @Override
    public long beginIteration() {
        if (this.wrappedEntries != null) {
            this.wrappedEntries.clear();
        }
        if (this.isEmpty()) {
            return 0;
        }
        if (this.containsNull) {
            return (long) this.n << 32 | this.size;
        }
        for (int pos = this.n; pos-- != 0; ) {
            K k = this.key[pos];
            //noinspection VariableNotUsedInsideIf
            if (k != null) {
                return (long) pos << 32 | this.size;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public K getIteration(long it) {
        int pos = (int) (it >> 32);
        if (pos >= 0) {
            return this.key[pos];
        }
        assert this.wrappedEntries != null;
        return this.wrappedEntries.get(-pos - 1);
    }

    @Override
    public K getSampleElement() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Empty set");
        }
        if (this.containsNull) {
            return this.key[this.n];
        }
        for (int pos = this.n; pos-- != 0; ) {
            K k = this.key[pos];
            if (k != null) {
                return k;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public ObjectIterator<K> iterator() {
        this.deprecatedMethod();
        return super.iterator();
    }

    @Override
    public long nextEntry(long it) {
        if (this.isEmpty()) {
            return 0;
        }
        int size = (int) it;
        if (--size == 0) {
            return 0;
        }
        int pos = (int) (it >> 32);
        final K[] key = this.key;
        while (true) {
            if (--pos < 0) {
                return (long) pos << 32 | size;
            }
            if (key[pos] != null) {
                return (long) pos << 32 | size;
            }
        }
    }

    @Override
    public long removeIteration(long it) {
        int pos = (int) (it >> 32);
        if (pos == this.n) {
            this.containsNull = false;
            this.key[this.n] = null;
        }
        else if (pos >= 0) {
            this.iterationShiftKeys(pos);
        }
        else {
            assert this.wrappedEntries != null;
            K wrappedEntry;
            try {
                wrappedEntry = this.wrappedEntries.set(-pos - 1, null);
            }
            catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException(e);
            }
            this.remove(wrappedEntry);
            return it;
        }
        --this.size;
        return it;
    }

    @Override
    public boolean trim() {
        if (this.wrappedEntries != null) {
            this.wrappedEntries.trim();
        }
        return super.trim();
    }

    @Override
    public @UnmodifiableView OSet<K> view() {
        if (this.view == null) {
            this.view = new View<>(this);
        }
        return this.view;
    }

    protected void iterationShiftKeys(int pos) {
        // Shift entries with the same hash.
        final K[] key = this.key;
        while (true) {
            int last;
            pos = (last = pos) + 1 & this.mask;
            K curr;
            while (true) {
                if ((curr = key[pos]) == null) {
                    key[last] = null;
                    return;
                }
                int slot = HashCommon.mix(curr.hashCode()) & this.mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
                    break;
                }
                pos = pos + 1 & this.mask;
            }
            if (pos < last) {
                if (this.wrappedEntries == null) {
                    this.wrappedEntries = new OArrayList<>(2);
                }
                this.wrappedEntries.add(key[pos]);
            }
            key[last] = curr;
        }
    }

    private void ensureCapacity(int capacity) {
        int needed = HashCommon.arraySize(capacity, this.f);
        if (needed > this.n) {
            this.rehash(needed);
        }
    }

    private void tryCapacity(long capacity) {
        int needed = (int) Math.min(0x4000_0000L, Math.max(2L, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity / this.f))));
        if (needed > this.n) {
            this.rehash(needed);
        }

    }
}
