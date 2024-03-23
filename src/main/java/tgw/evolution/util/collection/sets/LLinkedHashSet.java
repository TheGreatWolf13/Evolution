package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.Evolution;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LLinkedHashSet extends LongLinkedOpenHashSet implements LSet {

    protected @Nullable View view;

    public LLinkedHashSet(int expected, float f) {
        super(expected, f);
    }

    public LLinkedHashSet(int expected) {
        super(expected);
    }

    public LLinkedHashSet() {
    }

    public LLinkedHashSet(Collection<? extends Long> c, float f) {
        super(c, f);
    }

    public LLinkedHashSet(Collection<? extends Long> c) {
        super(c);
    }

    public LLinkedHashSet(LongCollection c, float f) {
        super(c, f);
    }

    public LLinkedHashSet(LongCollection c) {
        super(c);
    }

    public LLinkedHashSet(LongIterator i, float f) {
        super(i, f);
    }

    public LLinkedHashSet(LongIterator i) {
        super(i);
    }

    public LLinkedHashSet(Iterator<?> i, float f) {
        super(i, f);
    }

    public LLinkedHashSet(Iterator<?> i) {
        super(i);
    }

    public LLinkedHashSet(long[] a, int offset, int length, float f) {
        super(a, offset, length, f);
    }

    public LLinkedHashSet(long[] a, int offset, int length) {
        super(a, offset, length);
    }

    public LLinkedHashSet(long[] a, float f) {
        super(a, f);
    }

    public LLinkedHashSet(long[] a) {
        super(a);
    }

    @Override
    public long beginIteration() {
        if (this.isEmpty()) {
            return -1;
        }
        return this.first;
    }

    @Override
    public Long first() {
        Evolution.deprecatedMethod();
        return super.first();
    }

    @Override
    public long getIteration(long it) {
        int curr = (int) it;
        if (curr == -1) {
            throw new NoSuchElementException();
        }
        return this.key[curr];
    }

    @Override
    public long getSampleElement() {
        return this.firstLong();
    }

    @Override
    public boolean hasNextIteration(long it) {
        return (int) it != -1;
    }

    @Override
    public LongListIterator iterator() {
        this.deprecatedMethod();
        return super.iterator();
    }

    @Override
    public Long last() {
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
            this.key[this.n] = 0L;
        }
        else {
            long[] key = this.key;
            while (true) {
                int last = curr;
                curr = curr + 1 & this.mask;
                long currKey;
                while (true) {
                    if ((currKey = key[curr]) == 0L) {
                        key[last] = 0L;
                        return next | 1L << 63;
                    }
                    int slot = (int) HashCommon.mix(currKey) & this.mask;
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
    public @UnmodifiableView LSet view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
