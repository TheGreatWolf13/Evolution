package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleLinkedOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.Evolution;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DLinkedHashSet extends DoubleLinkedOpenHashSet implements DSet {

    protected @Nullable View view;

    public DLinkedHashSet(int expected, float f) {
        super(expected, f);
    }

    public DLinkedHashSet(int expected) {
        super(expected);
    }

    public DLinkedHashSet() {
    }

    public DLinkedHashSet(Collection<? extends Double> c, float f) {
        super(c, f);
    }

    public DLinkedHashSet(Collection<? extends Double> c) {
        super(c);
    }

    public DLinkedHashSet(DoubleCollection c, float f) {
        super(c, f);
    }

    public DLinkedHashSet(DoubleCollection c) {
        super(c);
    }

    public DLinkedHashSet(DoubleIterator i, float f) {
        super(i, f);
    }

    public DLinkedHashSet(DoubleIterator i) {
        super(i);
    }

    public DLinkedHashSet(Iterator<?> i, float f) {
        super(i, f);
    }

    public DLinkedHashSet(Iterator<?> i) {
        super(i);
    }

    public DLinkedHashSet(double[] a, int offset, int length, float f) {
        super(a, offset, length, f);
    }

    public DLinkedHashSet(double[] a, int offset, int length) {
        super(a, offset, length);
    }

    public DLinkedHashSet(double[] a, float f) {
        super(a, f);
    }

    public DLinkedHashSet(double[] a) {
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
    public Double first() {
        Evolution.deprecatedMethod();
        return super.first();
    }

    @Override
    public double getIteration(long it) {
        int curr = (int) it;
        if (curr == -1) {
            throw new NoSuchElementException();
        }
        return this.key[curr];
    }

    @Override
    public double getSampleElement() {
        return this.firstDouble();
    }

    @Override
    public boolean hasNextIteration(long it) {
        return (int) it != -1;
    }

    @Override
    public DoubleListIterator iterator() {
        this.deprecatedMethod();
        return super.iterator();
    }

    @Override
    public Double last() {
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
            this.key[this.n] = 0.0;
        }
        else {
            double[] key = this.key;
            while (true) {
                int last = curr;
                curr = curr + 1 & this.mask;
                double currKey;
                while (true) {
                    if (Double.doubleToLongBits(currKey = key[curr]) == 0L) {
                        key[last] = 0.0;
                        return next | 1L << 63;
                    }
                    int slot = HashCommon.mix(HashCommon.double2int(currKey)) & this.mask;
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
    public @UnmodifiableView DSet view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
