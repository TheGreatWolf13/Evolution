package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.lists.LArrayList;

import java.util.*;

public class LHashSet extends LongOpenHashSet implements LSet {

    protected @Nullable View view;
    protected @Nullable LArrayList wrappedEntries;

    public LHashSet(int expected, float f) {
        super(expected, f);
    }

    public LHashSet(int expected) {
        super(expected);
    }

    public LHashSet() {
    }

    public LHashSet(Collection<? extends Long> c, float f) {
        super(c, f);
    }

    public LHashSet(Collection<? extends Long> c) {
        super(c);
    }

    public LHashSet(LongCollection c, float f) {
        super(c, f);
    }

    public LHashSet(LongCollection c) {
        super(c);
    }

    public LHashSet(LongIterator i, float f) {
        super(i, f);
    }

    public LHashSet(LongIterator i) {
        super(i);
    }

    public LHashSet(Iterator<?> i, float f) {
        super(i, f);
    }

    public LHashSet(Iterator<?> i) {
        super(i);
    }

    public LHashSet(long[] a, int offset, int length, float f) {
        super(a, offset, length, f);
    }

    public LHashSet(long[] a, int offset, int length) {
        super(a, offset, length);
    }

    public LHashSet(long[] a, float f) {
        super(a, f);
    }

    public LHashSet(long[] a) {
        super(a);
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
            long k = this.key[pos];
            if (k != 0) {
                return (long) pos << 32 | this.size;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public long getIteration(long it) {
        int pos = (int) (it >> 32);
        if (pos >= 0) {
            return this.key[pos];
        }
        assert this.wrappedEntries != null;
        return this.wrappedEntries.get(-pos - 1);
    }

    @Override
    public long getSampleElement() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Empty set");
        }
        if (this.containsNull) {
            return this.key[this.n];
        }
        for (int pos = this.n; pos-- != 0; ) {
            long k = this.key[pos];
            if (k != 0) {
                return k;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    protected void iterationShiftKeys(int pos) {
        // Shift entries with the same hash.
        final long[] key = this.key;
        while (true) {
            int last;
            pos = (last = pos) + 1 & this.mask;
            long curr;
            while (true) {
                if ((curr = key[pos]) == 0L) {
                    key[last] = 0L;
                    return;
                }
                int slot = (int) HashCommon.mix(curr) & this.mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
                    break;
                }
                pos = pos + 1 & this.mask;
            }
            if (pos < last) {
                if (this.wrappedEntries == null) {
                    this.wrappedEntries = new LArrayList(2);
                }
                this.wrappedEntries.add(key[pos]);
            }
            key[last] = curr;
        }
    }

    @Override
    public LongIterator iterator() {
        this.deprecatedMethod();
        return super.iterator();
    }

    public void loadFrom(LHashSet set) {
        this.view = null;
        this.wrappedEntries = null;
        if (this.key.length < set.key.length) {
            this.key = new long[set.key.length];
        }
        System.arraycopy(set.key, 0, this.key, 0, set.key.length);
        if (this.key.length > set.key.length) {
            Arrays.fill(this.key, set.key.length, this.key.length, 0L);
        }
        this.containsNull = set.containsNull;
        this.mask = set.mask;
        this.n = set.n;
        this.maxFill = set.maxFill;
        this.size = set.size;
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
        final long[] key = this.key;
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
            long wrappedEntry;
            try {
                wrappedEntry = this.wrappedEntries.getLong(-pos - 1);
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
    public @UnmodifiableView LSet view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
