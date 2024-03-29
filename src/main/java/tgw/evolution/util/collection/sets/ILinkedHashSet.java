package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.Evolution;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ILinkedHashSet extends IntLinkedOpenHashSet implements ISet {

    protected @Nullable View view;

    public ILinkedHashSet(int expected, float f) {
        super(expected, f);
    }

    public ILinkedHashSet(int expected) {
        super(expected);
    }

    public ILinkedHashSet() {
    }

    public ILinkedHashSet(Collection<? extends Integer> c, float f) {
        super(c, f);
    }

    public ILinkedHashSet(Collection<? extends Integer> c) {
        super(c);
    }

    public ILinkedHashSet(IntCollection c, float f) {
        super(c, f);
    }

    public ILinkedHashSet(IntCollection c) {
        super(c);
    }

    public ILinkedHashSet(IntIterator i, float f) {
        super(i, f);
    }

    public ILinkedHashSet(IntIterator i) {
        super(i);
    }

    public ILinkedHashSet(Iterator<?> i, float f) {
        super(i, f);
    }

    public ILinkedHashSet(Iterator<?> i) {
        super(i);
    }

    public ILinkedHashSet(int[] a, int offset, int length, float f) {
        super(a, offset, length, f);
    }

    public ILinkedHashSet(int[] a, int offset, int length) {
        super(a, offset, length);
    }

    public ILinkedHashSet(int[] a, float f) {
        super(a, f);
    }

    public ILinkedHashSet(int[] a) {
        super(a);
    }

    @Override
    public boolean addAll(IntCollection c) {
        return ISet.super.addAll(c);
    }

    @Override
    public long beginIteration() {
        if (this.isEmpty()) {
            return -1;
        }
        return this.first;
    }

    @Override
    public Integer first() {
        Evolution.deprecatedMethod();
        return super.first();
    }

    @Override
    public int getIteration(long it) {
        int curr = (int) it;
        if (curr == -1) {
            throw new NoSuchElementException();
        }
        return this.key[curr];
    }

    @Override
    public int getSampleElement() {
        return this.firstInt();
    }

    @Override
    public boolean hasNextIteration(long it) {
        return (int) it != -1;
    }

    @Override
    public IntListIterator iterator() {
        this.deprecatedMethod();
        return super.iterator();
    }

    @Override
    public Integer last() {
        Evolution.deprecatedMethod();
        return super.last();
    }

    @Override
    public long nextEntry(long it) {
        int curr = (int) it;
        if ((it & 1L << 63) != 0) {
            return curr;
        }
        return (int) this.link[curr];
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
        int curr = (int) it;
        if (curr == -1) {
            throw new IllegalStateException();
        }
        long[] link = this.link;
        int next = (int) link[curr];
        int prev = (int) (link[curr] >>> 32);
        --this.size;
        //Fix pointers
        if (prev == -1) {
            this.first = next;
        }
        else {
            link[prev] ^= (link[prev] ^ next & 0xFFFF_FFFFL) & 0xFFFF_FFFFL;
        }
        if (next == -1) {
            this.last = prev;
        }
        else {
            link[next] ^= (link[next] ^ (prev & 0xFFFF_FFFFL) << 32) & 0xFFFF_FFFF_0000_0000L;
        }
        //Actually remove
        if (curr == this.n) {
            this.containsNull = false;
            this.key[this.n] = 0;
        }
        else {
            int[] key = this.key;
            while (true) {
                int last = curr;
                curr = curr + 1 & this.mask;
                int currKey;
                while (true) {
                    if ((currKey = key[curr]) == 0) {
                        key[last] = 0;
                        return next | 1L << 63;
                    }
                    int slot = HashCommon.mix(currKey) & this.mask;
                    if (last <= curr) {
                        if (last >= slot || slot > curr) {
                            break;
                        }
                    }
                    else if (last >= slot && slot > curr) {
                        break;
                    }
                    curr = curr + 1 & this.mask;
                }
                key[last] = currKey;
                if (next == curr) {
                    next = last;
                }
                this.fixPointers(curr, last);
            }
        }
        return next | 1L << 63;
    }

    @Override
    public @UnmodifiableView ISet view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
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
