package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.lists.IArrayList;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class IHashSet extends IntOpenHashSet implements ISet {

    protected @Nullable View view;
    protected @Nullable IArrayList wrappedEntries;

    public IHashSet(int expected, float f) {
        super(expected, f);
    }

    public IHashSet(int expected) {
        super(expected);
    }

    public IHashSet() {
    }

    public IHashSet(Collection<? extends Integer> c, float f) {
        super(c, f);
    }

    public IHashSet(Collection<? extends Integer> c) {
        super(c);
    }

    public IHashSet(IntCollection c, float f) {
        super(c, f);
    }

    public IHashSet(IntCollection c) {
        super(c);
    }

    public IHashSet(IntIterator i, float f) {
        super(i, f);
    }

    public IHashSet(IntIterator i) {
        super(i);
    }

    public IHashSet(Iterator<?> i, float f) {
        super(i, f);
    }

    public IHashSet(Iterator<?> i) {
        super(i);
    }

    public IHashSet(int[] a, int offset, int length, float f) {
        super(a, offset, length, f);
    }

    public IHashSet(int[] a, int offset, int length) {
        super(a, offset, length);
    }

    public IHashSet(int[] a, float f) {
        super(a, f);
    }

    public IHashSet(int[] a) {
        super(a);
    }

    @Override
    public boolean addAll(IntCollection c) {
        return ISet.super.addAll(c);
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
            int k = this.key[pos];
            if (k != 0) {
                return (long) pos << 32 | this.size;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public int getIteration(long it) {
        int pos = (int) (it >> 32);
        if (pos >= 0) {
            return this.key[pos];
        }
        assert this.wrappedEntries != null;
        return this.wrappedEntries.get(-pos - 1);
    }

    @Override
    public int getSampleElement() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Empty set");
        }
        if (this.containsNull) {
            return this.key[this.n];
        }
        for (int pos = this.n; pos-- != 0; ) {
            int k = this.key[pos];
            if (k != 0) {
                return k;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public IntIterator iterator() {
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
        final int[] key = this.key;
        while (true) {
            if (--pos < 0) {
                return (long) pos << 32 | size;
            }
            if (key[pos] != 0) {
                return (long) pos << 32 | size;
            }
        }
    }

    @Override
    public void preAllocate(int extraSize) {
        if (this.f <= 0.5) {
            this.ensureCapacity(extraSize);
        }
        else {
            this.tryCapacity(this.size() + extraSize);
        }
    }

    @Override
    public long removeIteration(long it) {
        int pos = (int) (it >> 32);
        if (pos == this.n) {
            this.containsNull = false;
            this.key[this.n] = 0;
        }
        else if (pos >= 0) {
            this.iterationShiftKeys(pos);
        }
        else {
            assert this.wrappedEntries != null;
            int wrappedEntry;
            try {
                wrappedEntry = this.wrappedEntries.getInt(-pos - 1);
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
    public @UnmodifiableView ISet view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }

    protected void iterationShiftKeys(int pos) {
        // Shift entries with the same hash.
        final int[] key = this.key;
        while (true) {
            int last;
            pos = (last = pos) + 1 & this.mask;
            int curr;
            while (true) {
                if ((curr = key[pos]) == 0) {
                    key[last] = 0;
                    return;
                }
                int slot = HashCommon.mix(curr) & this.mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
                    break;
                }
                pos = pos + 1 & this.mask;
            }
            if (pos < last) {
                if (this.wrappedEntries == null) {
                    this.wrappedEntries = new IArrayList(2);
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
        int needed = (int) Math.min(1_073_741_824L, Math.max(2L, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity / this.f))));
        if (needed > this.n) {
            this.rehash(needed);
        }
    }
}
