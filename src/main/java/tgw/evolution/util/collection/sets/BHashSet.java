package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.lists.BArrayList;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class BHashSet extends ByteOpenHashSet implements BSet {

    protected @Nullable View view;
    protected @Nullable BArrayList wrappedEntries;

    public BHashSet(int expected, float f) {
        super(expected, f);
    }

    public BHashSet(int expected) {
        super(expected);
    }

    public BHashSet() {
    }

    public BHashSet(Collection<? extends Byte> c, float f) {
        super(c, f);
    }

    public BHashSet(Collection<? extends Byte> c) {
        super(c);
    }

    public BHashSet(ByteCollection c, float f) {
        super(c, f);
    }

    public BHashSet(ByteCollection c) {
        super(c);
    }

    public BHashSet(ByteIterator i, float f) {
        super(i, f);
    }

    public BHashSet(ByteIterator i) {
        super(i);
    }

    public BHashSet(Iterator<?> i, float f) {
        super(i, f);
    }

    public BHashSet(Iterator<?> i) {
        super(i);
    }

    public BHashSet(byte[] a, int offset, int length, float f) {
        super(a, offset, length, f);
    }

    public BHashSet(byte[] a, int offset, int length) {
        super(a, offset, length);
    }

    public BHashSet(byte[] a, float f) {
        super(a, f);
    }

    public BHashSet(byte[] a) {
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
            byte k = this.key[pos];
            if (k != 0) {
                return (long) pos << 32 | this.size;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public byte getIteration(long it) {
        int pos = (int) (it >> 32);
        if (pos >= 0) {
            return this.key[pos];
        }
        assert this.wrappedEntries != null;
        return this.wrappedEntries.get(-pos - 1);
    }

    @Override
    public byte getSampleElement() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Empty set");
        }
        if (this.containsNull) {
            return this.key[this.n];
        }
        for (int pos = this.n; pos-- != 0; ) {
            byte k = this.key[pos];
            if (k != 0) {
                return k;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    protected void iterationShiftKeys(int pos) {
        // Shift entries with the same hash.
        final byte[] key = this.key;
        while (true) {
            int last;
            pos = (last = pos) + 1 & this.mask;
            byte curr;
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
                    this.wrappedEntries = new BArrayList(2);
                }
                this.wrappedEntries.add(key[pos]);
            }
            key[last] = curr;
        }
    }

    @Override
    public ByteIterator iterator() {
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
        final byte[] key = this.key;
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
    public void removeIteration(long it) {
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
            byte wrappedEntry;
            try {
                wrappedEntry = this.wrappedEntries.getByte(-pos - 1);
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
    public @UnmodifiableView BSet view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
