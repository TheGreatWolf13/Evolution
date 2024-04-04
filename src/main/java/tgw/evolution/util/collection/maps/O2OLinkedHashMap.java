package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.NoSuchElementException;

public class O2OLinkedHashMap<K, V> extends Object2ObjectLinkedOpenHashMap<K, V> implements O2OMap<K, V> {

    protected @Nullable View<K, V> view;

    public O2OLinkedHashMap(int expected, float f) {
        super(expected, f);
    }

    public O2OLinkedHashMap(int expected) {
        super(expected);
    }

    public O2OLinkedHashMap() {
    }

    public O2OLinkedHashMap(Map<? extends K, ? extends V> m, float f) {
        super(m, f);
    }

    public O2OLinkedHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public O2OLinkedHashMap(Object2ObjectMap<K, V> m, float f) {
        super(m, f);
    }

    public O2OLinkedHashMap(Object2ObjectMap<K, V> m) {
        super(m);
    }

    public O2OLinkedHashMap(K[] k, V[] v, float f) {
        super(k, v, f);
    }

    public O2OLinkedHashMap(K[] k, V[] v) {
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
    public K getIterationKey(long it) {
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
    public K getSampleKey() {
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
    public ObjectSortedSet<K> keySet() {
        this.deprecatedMethod();
        return super.keySet();
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
    public FastSortedEntrySet<K, V> object2ObjectEntrySet() {
        this.deprecatedMethod();
        return super.object2ObjectEntrySet();
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
    public void putAll(Map<? extends K, ? extends V> m) {
        O2OMap.super.putAll(m);
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
            this.key[this.n] = null;
            this.value[this.n] = null;
        }
        else {
            K[] key = this.key;
            V[] value = this.value;
            while (true) {
                int last = curr;
                curr = curr + 1 & this.mask;
                K currKey;
                while (true) {
                    if ((currKey = key[curr]) == null) {
                        key[last] = null;
                        value[last] = null;
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
    public @UnmodifiableView O2OMap<K, V> view() {
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
