package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.lists.OArrayList;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.NoSuchElementException;

public class O2OHashMap<K, V> extends Object2ObjectOpenHashMap<K, V> implements O2OMap<K, V> {

    protected @Nullable View<K, V> view;
    protected @Nullable OArrayList<K> wrappedEntries;

    public O2OHashMap(int expected, float f) {
        super(expected, f);
    }

    public O2OHashMap(int expected) {
        super(expected);
    }

    public O2OHashMap() {
    }

    public O2OHashMap(Map<? extends K, ? extends V> m, float f) {
        super(m, f);
    }

    public O2OHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public O2OHashMap(Object2ObjectMap<K, V> m, float f) {
        super(m, f);
    }

    public O2OHashMap(Object2ObjectMap<K, V> m) {
        super(m);
    }

    public O2OHashMap(K[] k, V[] v, float f) {
        super(k, v, f);
    }

    public O2OHashMap(K[] k, V[] v) {
        super(k, v);
    }

    @Override
    public long beginIteration() {
        if (this.wrappedEntries != null) {
            this.wrappedEntries.clear();
        }
        if (this.isEmpty()) {
            return 0;
        }
        if (this.containsNullKey) {
            return (long) this.n << 32 | this.size;
        }
        for (int pos = this.n; pos-- != 0; ) {
            if (this.key[pos] != null) {
                return (long) pos << 32 | this.size;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public K getIterationKey(long it) {
        int pos = (int) (it >> 32);
        if (pos >= 0) {
            return this.key[pos];
        }
        assert this.wrappedEntries != null;
        return this.wrappedEntries.get(-pos - 1);
    }

    @Override
    public V getIterationValue(long it) {
        int pos = (int) (it >> 32);
        if (pos >= 0) {
            return this.value[pos];
        }
        assert this.wrappedEntries != null;
        K k = this.wrappedEntries.get(-pos - 1);
        int p = HashCommon.mix(k.hashCode()) & this.mask;
        while (k != this.key[p]) {
            p = p + 1 & this.mask;
        }
        return this.value[p];
    }

    @Override
    public K getSampleKey() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Empty set");
        }
        if (this.containsNullKey) {
            return this.key[this.n];
        }
        for (int pos = this.n; pos-- != 0; ) {
            K k = this.key[pos];
            if (k != null) {
                return k;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public V getSampleValue() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Empty set");
        }
        if (this.containsNullKey) {
            return this.value[this.n];
        }
        for (int pos = this.n; pos-- != 0; ) {
            if (this.key[pos] != null) {
                return this.value[pos];
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public ObjectSet<K> keySet() {
        this.deprecatedMethod();
        return super.keySet();
    }

    @Override
    public long nextEntry(long it) {
        if (this.isEmpty()) {
            return 0;
        }
        int size = (int) it;
        if (--size == 0) {
            return 0;
        }
        int pos = (int) (it >> 32);
        final K[] key = this.key;
        while (true) {
            if (--pos < 0) {
                return (long) pos << 32 | size;
            }
            if (key[pos] != null) {
                return (long) pos << 32 | size;
            }
        }
    }

    @Override
    public FastEntrySet<K, V> object2ObjectEntrySet() {
        this.deprecatedMethod();
        return super.object2ObjectEntrySet();
    }

    @Override
    public long removeIteration(long it) {
        int pos = (int) (it >> 32);
        if (pos == this.n) {
            this.containsNullKey = false;
            this.key[this.n] = null;
            this.value[this.n] = null;
        }
        else if (pos >= 0) {
            this.iterationShiftKeys(pos);
        }
        else {
            assert this.wrappedEntries != null;
            K wrappedEntry;
            try {
                wrappedEntry = this.wrappedEntries.set(-pos - 1, null);
            }
            catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException(e);
            }
            this.remove(wrappedEntry);
            return it;
        }
        --this.size;
        return it;
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

    protected void iterationShiftKeys(int pos) {
        final K[] key = this.key;
        while (true) {
            int last = pos;
            pos = pos + 1 & this.mask;
            K curr;
            while (true) {
                if ((curr = key[pos]) == null) {
                    key[last] = null;
                    this.value[last] = null;
                    return;
                }
                int slot = HashCommon.mix(curr.hashCode()) & this.mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
                    break;
                }
                pos = pos + 1 & this.mask;
            }
            if (pos < last) {
                if (this.wrappedEntries == null) {
                    this.wrappedEntries = new OArrayList<>(2);
                }
                this.wrappedEntries.add(key[pos]);
            }
            key[last] = curr;
            this.value[last] = this.value[pos];
        }
    }
}
