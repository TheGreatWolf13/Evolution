package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatIterator;
import it.unimi.dsi.fastutil.floats.FloatOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.lists.FArrayList;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FHashSet extends FloatOpenHashSet implements FSet {

    protected @Nullable View view;
    protected @Nullable FArrayList wrappedEntries;

    public FHashSet(int expected, float f) {
        super(expected, f);
    }

    public FHashSet(int expected) {
        super(expected);
    }

    public FHashSet() {
    }

    public FHashSet(Collection<? extends Float> c, float f) {
        super(c, f);
    }

    public FHashSet(Collection<? extends Float> c) {
        super(c);
    }

    public FHashSet(FloatCollection c, float f) {
        super(c, f);
    }

    public FHashSet(FloatCollection c) {
        super(c);
    }

    public FHashSet(FloatIterator i, float f) {
        super(i, f);
    }

    public FHashSet(FloatIterator i) {
        super(i);
    }

    public FHashSet(Iterator<?> i, float f) {
        super(i, f);
    }

    public FHashSet(Iterator<?> i) {
        super(i);
    }

    public FHashSet(float[] a, int offset, int length, float f) {
        super(a, offset, length, f);
    }

    public FHashSet(float[] a, int offset, int length) {
        super(a, offset, length);
    }

    public FHashSet(float[] a, float f) {
        super(a, f);
    }

    public FHashSet(float[] a) {
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
            float k = this.key[pos];
            if (Float.floatToIntBits(k) != 0) {
                return (long) pos << 32 | this.size;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public float getIteration(long it) {
        int pos = (int) (it >> 32);
        if (pos >= 0) {
            return this.key[pos];
        }
        assert this.wrappedEntries != null;
        return this.wrappedEntries.get(-pos - 1);
    }

    @Override
    public float getSampleElement() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Empty set");
        }
        if (this.containsNull) {
            return this.key[this.n];
        }
        for (int pos = this.n; pos-- != 0; ) {
            float k = this.key[pos];
            if (Float.floatToIntBits(k) != 0) {
                return k;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    protected void iterationShiftKeys(int pos) {
        // Shift entries with the same hash.
        final float[] key = this.key;
        while (true) {
            int last;
            pos = (last = pos) + 1 & this.mask;
            float curr;
            while (true) {
                if (Float.floatToIntBits(curr = key[pos]) == 0) {
                    key[last] = 0.0f;
                    return;
                }
                int slot = HashCommon.mix(HashCommon.float2int(curr)) & this.mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
                    break;
                }
                pos = pos + 1 & this.mask;
            }
            if (pos < last) {
                if (this.wrappedEntries == null) {
                    this.wrappedEntries = new FArrayList(2);
                }
                this.wrappedEntries.add(key[pos]);
            }
            key[last] = curr;
        }
    }

    @Override
    public FloatIterator iterator() {
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
        final float[] key = this.key;
        while (true) {
            if (--pos < 0) {
                return (long) pos << 32 | size;
            }
            if (Float.floatToIntBits(key[pos]) != 0) {
                return (long) pos << 32 | size;
            }
        }
    }

    @Override
    public void removeIteration(long it) {
        int pos = (int) (it >> 32);
        if (pos == this.n) {
            this.containsNull = false;
            this.key[this.n] = 0.0f;
        }
        else if (pos >= 0) {
            this.iterationShiftKeys(pos);
        }
        else {
            assert this.wrappedEntries != null;
            float wrappedEntry;
            try {
                wrappedEntry = this.wrappedEntries.getFloat(-pos - 1);
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
    public @UnmodifiableView FSet view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
