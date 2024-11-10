package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.NoSuchElementException;

public class L2OLinkedHashMap<V> extends Long2ObjectLinkedOpenHashMap<V> implements L2OMap<V> {

    protected @Nullable View<V> view;

    public L2OLinkedHashMap(int expected, float f) {
        super(expected, f);
    }

    public L2OLinkedHashMap(int expected) {
        super(expected);
    }

    public L2OLinkedHashMap() {
    }

    public L2OLinkedHashMap(Map<Long, ? extends V> m, float f) {
        super(m, f);
    }

    public L2OLinkedHashMap(Map<Long, ? extends V> m) {
        super(m);
    }

    public L2OLinkedHashMap(Long2ObjectMap<V> m, float f) {
        super(m, f);
    }

    public L2OLinkedHashMap(Long2ObjectMap<V> m) {
        super(m);
    }

    public L2OLinkedHashMap(long[] k, V[] v, float f) {
        super(k, v, f);
    }

    public L2OLinkedHashMap(long[] k, V[] v) {
        super(k, v);
    }

    @Override
    public long beginIteration() {
        if (this.isEmpty()) {
            return -1;
        }
        return this.first;
    }

    @Override
    public long getIterationKey(long it) {
        int curr = (int) it;
        if (curr == -1) {
            throw new NoSuchElementException();
        }
        return this.key[curr];
    }

    @Override
    public V getIterationValue(long it) {
        int curr = (int) it;
        if (curr == -1) {
            throw new NoSuchElementException();
        }
        return this.value[curr];
    }

    @Override
    public long getSampleKey() {
        return this.firstKey();
    }

    @Override
    public V getSampleValue() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Empty map");
        }
        return this.value[this.first];
    }

    @Override
    public boolean hasNextIteration(long it) {
        return (int) it != -1;
    }

    @Override
    public LongSortedSet keySet() {
        this.deprecatedMethod();
        return super.keySet();
    }

    @Override
    public FastSortedEntrySet<V> long2ObjectEntrySet() {
        this.deprecatedMethod();
        return super.long2ObjectEntrySet();
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
    public void putAll(Map<? extends Long, ? extends V> m) {
        L2OMap.super.putAll(m);
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
            this.containsNullKey = false;
            this.key[this.n] = 0;
            this.value[this.n] = null;
        }
        else {
            long[] key = this.key;
            V[] value = this.value;
            while (true) {
                int last = curr;
                curr = curr + 1 & this.mask;
                long currKey;
                while (true) {
                    if ((currKey = key[curr]) == 0) {
                        key[last] = 0;
                        value[last] = null;
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
                value[last] = value[curr];
                if (next == curr) {
                    next = last;
                }
                this.fixPointers(curr, last);
            }
        }
        return next | 1L << 63;
    }

    @Override
    public ObjectCollection<V> values() {
        this.deprecatedMethod();
        return super.values();
    }

    @Override
    public @UnmodifiableView L2OMap<V> view() {
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
        int needed = (int) Math.min(1_073_741_824L, Math.max(2L, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity / this.f))));
        if (needed > this.n) {
            this.rehash(needed);
        }
    }
}
