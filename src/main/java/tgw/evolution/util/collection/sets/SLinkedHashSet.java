package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortLinkedOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.Evolution;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SLinkedHashSet extends ShortLinkedOpenHashSet implements SSet {

    protected @Nullable View view;

    public SLinkedHashSet(int expected, float f) {
        super(expected, f);
    }

    public SLinkedHashSet(int expected) {
        super(expected);
    }

    public SLinkedHashSet() {
    }

    public SLinkedHashSet(Collection<? extends Short> c, float f) {
        super(c, f);
    }

    public SLinkedHashSet(Collection<? extends Short> c) {
        super(c);
    }

    public SLinkedHashSet(ShortCollection c, float f) {
        super(c, f);
    }

    public SLinkedHashSet(ShortCollection c) {
        super(c);
    }

    public SLinkedHashSet(ShortIterator i, float f) {
        super(i, f);
    }

    public SLinkedHashSet(ShortIterator i) {
        super(i);
    }

    public SLinkedHashSet(Iterator<?> i, float f) {
        super(i, f);
    }

    public SLinkedHashSet(Iterator<?> i) {
        super(i);
    }

    public SLinkedHashSet(short[] a, int offset, int length, float f) {
        super(a, offset, length, f);
    }

    public SLinkedHashSet(short[] a, int offset, int length) {
        super(a, offset, length);
    }

    public SLinkedHashSet(short[] a, float f) {
        super(a, f);
    }

    public SLinkedHashSet(short[] a) {
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
    public Short first() {
        Evolution.deprecatedMethod();
        return super.first();
    }

    @Override
    public short getIteration(long it) {
        int curr = (int) it;
        if (curr == -1) {
            throw new NoSuchElementException();
        }
        return this.key[curr];
    }

    @Override
    public short getSampleElement() {
        return this.firstShort();
    }

    @Override
    public boolean hasNextIteration(long it) {
        return (int) it != -1;
    }

    @Override
    public ShortListIterator iterator() {
        this.deprecatedMethod();
        return super.iterator();
    }

    @Override
    public Short last() {
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
            this.key[this.n] = 0;
        }
        else {
            short[] key = this.key;
            while (true) {
                int last = curr;
                curr = curr + 1 & this.mask;
                short currKey;
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
    public @UnmodifiableView SSet view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
