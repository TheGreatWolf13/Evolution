package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.lists.DArrayList;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DHashSet extends DoubleOpenHashSet implements DSet {

    protected @Nullable View view;
    protected @Nullable DArrayList wrappedEntries;

    public DHashSet(int expected, float f) {
        super(expected, f);
    }

    public DHashSet(int expected) {
        super(expected);
    }

    public DHashSet() {
    }

    public DHashSet(Collection<? extends Double> c, float f) {
        super(c, f);
    }

    public DHashSet(Collection<? extends Double> c) {
        super(c);
    }

    public DHashSet(DoubleCollection c, float f) {
        super(c, f);
    }

    public DHashSet(DoubleCollection c) {
        super(c);
    }

    public DHashSet(DoubleIterator i, float f) {
        super(i, f);
    }

    public DHashSet(DoubleIterator i) {
        super(i);
    }

    public DHashSet(Iterator<?> i, float f) {
        super(i, f);
    }

    public DHashSet(Iterator<?> i) {
        super(i);
    }

    public DHashSet(double[] a, int offset, int length, float f) {
        super(a, offset, length, f);
    }

    public DHashSet(double[] a, int offset, int length) {
        super(a, offset, length);
    }

    public DHashSet(double[] a, float f) {
        super(a, f);
    }

    public DHashSet(double[] a) {
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
            double k = this.key[pos];
            if (Double.doubleToLongBits(k) != 0L) {
                return (long) pos << 32 | this.size;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public double getIteration(long it) {
        int pos = (int) (it >> 32);
        if (pos >= 0) {
            return this.key[pos];
        }
        assert this.wrappedEntries != null;
        return this.wrappedEntries.get(-pos - 1);
    }

    @Override
    public double getSampleElement() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Empty set");
        }
        if (this.containsNull) {
            return this.key[this.n];
        }
        for (int pos = this.n; pos-- != 0; ) {
            double k = this.key[pos];
            if (Double.doubleToLongBits(k) != 0L) {
                return k;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    protected void iterationShiftKeys(int pos) {
        // Shift entries with the same hash.
        final double[] key = this.key;
        while (true) {
            int last;
            pos = (last = pos) + 1 & this.mask;
            double curr;
            while (true) {
                if (Double.doubleToLongBits(curr = key[pos]) == 0L) {
                    key[last] = 0.0;
                    return;
                }
                int slot = HashCommon.mix(HashCommon.double2int(curr)) & this.mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
                    break;
                }
                pos = pos + 1 & this.mask;
            }
            if (pos < last) {
                if (this.wrappedEntries == null) {
                    this.wrappedEntries = new DArrayList(2);
                }
                this.wrappedEntries.add(key[pos]);
            }
            key[last] = curr;
        }
    }

    @Override
    public DoubleIterator iterator() {
        this.deprecatedSetMethod();
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
        final double[] key = this.key;
        while (true) {
            if (--pos < 0) {
                return (long) pos << 32 | size;
            }
            if (Double.doubleToLongBits(key[pos]) != 0L) {
                return (long) pos << 32 | size;
            }
        }
    }

    @Override
    public void removeIteration(long it) {
        int pos = (int) (it >> 32);
        if (pos == this.n) {
            this.containsNull = false;
            this.key[this.n] = 0.0;
        }
        else if (pos >= 0) {
            this.iterationShiftKeys(pos);
        }
        else {
            assert this.wrappedEntries != null;
            double wrappedEntry;
            try {
                wrappedEntry = this.wrappedEntries.getDouble(-pos - 1);
            }
            catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException(e);
            }
            this.remove(wrappedEntry);
            return;
        }
        --this.size;
    }

    @Override
    public void trimCollection() {
        this.trim();
        if (this.wrappedEntries != null) {
            this.wrappedEntries.trim();
        }
    }

    @Override
    public @UnmodifiableView DSet view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
