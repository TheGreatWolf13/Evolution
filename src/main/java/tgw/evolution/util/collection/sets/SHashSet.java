package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.lists.SArrayList;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SHashSet extends ShortOpenHashSet implements SSet {

    protected @Nullable View view;
    protected @Nullable SArrayList wrappedEntries;

    public SHashSet(int expected, float f) {
        super(expected, f);
    }

    public SHashSet(int expected) {
        super(expected);
    }

    public SHashSet() {
    }

    public SHashSet(Collection<? extends Short> c, float f) {
        super(c, f);
    }

    public SHashSet(Collection<? extends Short> c) {
        super(c);
    }

    public SHashSet(ShortCollection c, float f) {
        super(c, f);
    }

    public SHashSet(ShortCollection c) {
        super(c);
    }

    public SHashSet(ShortIterator i, float f) {
        super(i, f);
    }

    public SHashSet(ShortIterator i) {
        super(i);
    }

    public SHashSet(Iterator<?> i, float f) {
        super(i, f);
    }

    public SHashSet(Iterator<?> i) {
        super(i);
    }

    public SHashSet(short[] a, int offset, int length, float f) {
        super(a, offset, length, f);
    }

    public SHashSet(short[] a, int offset, int length) {
        super(a, offset, length);
    }

    public SHashSet(short[] a, float f) {
        super(a, f);
    }

    public SHashSet(short[] a) {
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
            short k = this.key[pos];
            if (k != 0) {
                return (long) pos << 32 | this.size;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public short getIteration(long it) {
        int pos = (int) (it >> 32);
        if (pos >= 0) {
            return this.key[pos];
        }
        assert this.wrappedEntries != null;
        return this.wrappedEntries.get(-pos - 1);
    }

    @Override
    public short getSampleElement() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Empty set");
        }
        if (this.containsNull) {
            return this.key[this.n];
        }
        for (int pos = this.n; pos-- != 0; ) {
            short k = this.key[pos];
            if (k != 0) {
                return k;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public ShortIterator iterator() {
        this.deprecatedMethod();
        return super.iterator();
    }

    @Override
    public long nextEntry(long it) {
        if (this.isEmpty()) {
            return 0;
        }
        int size = (int) (it & ITERATION_END);
        if (--size == 0) {
            return 0;
        }
        int pos = (int) (it >> 32);
        final short[] key = this.key;
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
            short wrappedEntry;
            try {
                wrappedEntry = this.wrappedEntries.getShort(-pos - 1);
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
    public @UnmodifiableView SSet view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }

    protected void iterationShiftKeys(int pos) {
        // Shift entries with the same hash.
        final short[] key = this.key;
        while (true) {
            int last;
            pos = (last = pos) + 1 & this.mask;
            short curr;
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
                    this.wrappedEntries = new SArrayList(2);
                }
                this.wrappedEntries.add(key[pos]);
            }
            key[last] = curr;
        }
    }
}
