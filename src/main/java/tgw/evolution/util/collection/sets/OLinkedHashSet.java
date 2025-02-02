package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class OLinkedHashSet<K> extends ObjectLinkedOpenHashSet<K> implements OSet<K> {

    protected @Nullable View<K> view;

    public OLinkedHashSet(int expected, float f) {
        super(expected, f);
    }

    public OLinkedHashSet(int expected) {
        super(expected);
    }

    public OLinkedHashSet() {
    }

    public OLinkedHashSet(Collection<? extends K> c, float f) {
        super(c, f);
    }

    public OLinkedHashSet(Collection<? extends K> c) {
        super(c);
    }

    public OLinkedHashSet(ObjectCollection<? extends K> c, float f) {
        super(c, f);
    }

    public OLinkedHashSet(ObjectCollection<? extends K> c) {
        super(c);
    }

    public OLinkedHashSet(Iterator<? extends K> i, float f) {
        super(i, f);
    }

    public OLinkedHashSet(Iterator<? extends K> i) {
        super(i);
    }

    public OLinkedHashSet(K[] a, int offset, int length, float f) {
        super(a, offset, length, f);
    }

    public OLinkedHashSet(K[] a, int offset, int length) {
        super(a, offset, length);
    }

    public OLinkedHashSet(K[] a, float f) {
        super(a, f);
    }

    public OLinkedHashSet(K[] a) {
        super(a);
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        return OSet.super.addAll(c);
    }

    @Override
    public long beginIteration() {
        if (this.isEmpty()) {
            return -1;
        }
        return this.first;
    }

    @Override
    public K getIteration(long it) {
        int curr = (int) it;
        if (curr == -1) {
            throw new NoSuchElementException();
        }
        return this.key[curr];
    }

    @Override
    public K getSampleElement() {
        return this.first();
    }

    @Override
    public boolean hasNextIteration(long it) {
        return (int) it != -1;
    }

    @Override
    public ObjectListIterator<K> iterator() {
        this.deprecatedMethod();
        return super.iterator();
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
            this.key[this.n] = null;
        }
        else {
            K[] key = this.key;
            while (true) {
                int last = curr;
                curr = curr + 1 & this.mask;
                K currKey;
                while (true) {
                    if ((currKey = key[curr]) == null) {
                        key[last] = null;
                        return next | 1L << 63;
                    }
                    int slot = HashCommon.mix(currKey.hashCode()) & this.mask;
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
    public @UnmodifiableView OSet<K> view() {
        if (this.view == null) {
            this.view = new View<>(this);
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
        int needed = (int) Math.min(0x4000_0000L, Math.max(2L, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity / this.f))));
        if (needed > this.n) {
            this.rehash(needed);
        }
    }
}
