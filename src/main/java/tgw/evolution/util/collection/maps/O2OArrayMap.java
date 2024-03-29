package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Consumer;

public class O2OArrayMap<K, V> extends AbstractObject2ObjectMap<K, V> implements O2OMap<K, V> {

    protected @Nullable Object2ObjectMap.FastEntrySet<K, V> entries;
    protected K[] key;
    protected @Nullable ObjectSet<K> keys;
    protected int size;
    protected V[] value;
    protected @Nullable ObjectCollection<V> values;
    protected @Nullable View<K, V> view;

    public O2OArrayMap(K[] key, V[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public O2OArrayMap() {
        this.key = (K[]) ObjectArrays.EMPTY_ARRAY;
        this.value = (V[]) ObjectArrays.EMPTY_ARRAY;
    }

    public O2OArrayMap(int capacity) {
        this.key = (K[]) new Object[capacity];
        this.value = (V[]) new Object[capacity];
    }

    public O2OArrayMap(O2OMap<K, V> m) {
        this(m.size());
        int i = 0;
        for (ObjectIterator<Entry<K, V>> it = m.object2ObjectEntrySet().iterator(); it.hasNext(); ++i) {
            Entry<K, V> e = it.next();
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
        }
        this.size = i;
    }

    public O2OArrayMap(Map<? extends K, ? extends V> m) {
        this(m.size());
        int i = 0;
        for (var it = m.entrySet().iterator(); it.hasNext(); ++i) {
            Map.Entry<? extends K, ? extends V> e = it.next();
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
        }
        this.size = i;
    }

    public O2OArrayMap(K[] key, V[] value, int size) {
        this.key = key;
        this.value = value;
        this.size = size;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
        if (size > key.length) {
            throw new IllegalArgumentException("The provided size (" + size + ") is larger than or equal to the backing-arrays size (" + key.length + ")");
        }
    }

    @Override
    public long beginIteration() {
        return this.size;
    }

    @Override
    public void clear() {
        Arrays.fill(this.key, 0, this.size, null);
        Arrays.fill(this.value, 0, this.size, null);
        this.size = 0;
    }

    @Override
    public boolean containsKey(Object k) {
        return this.findKey(k) != -1;
    }

    @Override
    public boolean containsValue(Object v) {
        int i = this.size;
        if (i == 0) {
            return false;
        }
        while (!Objects.equals(this.value[--i], v)) {
            if (i == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @Nullable V get(Object k) {
        K[] key = this.key;
        int i = this.size;
        if (i == 0) {
            return this.defRetValue;
        }
        while (!Objects.equals(key[--i], k)) {
            if (i == 0) {
                return this.defRetValue;
            }
        }
        return this.value[i];
    }

    @Override
    public K getIterationKey(long it) {
        int pos = (int) (it >> 32);
        return this.key[pos];
    }

    @Override
    public V getIterationValue(long it) {
        int pos = (int) (it >> 32);
        return this.value[pos];
    }

    @Override
    public K getSampleKey() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Map is empty!");
        }
        return this.key[0];
    }

    @Override
    public V getSampleValue() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Map is empty!");
        }
        return this.value[0];
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public ObjectSet<K> keySet() {
        if (this.keys == null) {
            this.keys = new KeySet();
        }
        return this.keys;
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
        int pos = (int) (it >> 32) + 1;
        return (long) pos << 32 | size;
    }

    @Override
    public Object2ObjectMap.FastEntrySet<K, V> object2ObjectEntrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }

        return this.entries;
    }

    @Override
    public V put(K k, V v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            V oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            K[] newKey = (K[]) new Object[this.size == 0 ? 2 : this.size * 2];
            V[] newValue = (V[]) new Object[this.size == 0 ? 2 : this.size * 2];
            for (int i = this.size; i-- != 0; newValue[i] = this.value[i]) {
                newKey[i] = this.key[i];
            }
            this.key = newKey;
            this.value = newValue;
        }
        this.key[this.size] = k;
        this.value[this.size] = v;
        ++this.size;
        return this.defRetValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        O2OMap.super.putAll(m);
    }

    @Override
    public V remove(Object k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        V oldValue = this.value[oldPos];
        int tail = this.size - oldPos - 1;
        System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
        System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
        --this.size;
        this.key[this.size] = null;
        this.value[this.size] = null;
        return oldValue;
    }

    @Override
    public long removeIteration(long it) {
        int pos = (int) (it >> 32);
        int size = (int) it;
        int tail = this.size - pos - 1;
        System.arraycopy(this.key, pos + 1, this.key, pos, tail);
        System.arraycopy(this.value, pos + 1, this.value, pos, tail);
        --this.size;
        this.key[this.size] = null;
        this.value[this.size] = null;
        return (long) --pos << 32 | size;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean trim() {
        if (this.key.length > this.size) {
            K[] newKey = (K[]) new Object[this.size];
            V[] newValue = (V[]) new Object[this.size];
            System.arraycopy(this.key, 0, newKey, 0, this.size);
            System.arraycopy(this.value, 0, newValue, 0, this.size);
            this.key = newKey;
            this.value = newValue;
            return true;
        }
        return false;
    }

    @Override
    public ObjectCollection<V> values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    @Override
    public @UnmodifiableView O2OMap<K, V> view() {
        if (this.view == null) {
            this.view = new View<>(this);
        }
        return this.view;
    }

    private int findKey(Object k) {
        K[] key = this.key;
        int i = this.size;
        if (i == 0) {
            return -1;
        }
        while (!Objects.equals(key[--i], k)) {
            if (i == 0) {
                return -1;
            }
        }
        return i;
    }

    public static class BasicEntry<K, V> implements Object2ObjectMap.Entry<K, V> {
        protected @Nullable K key;
        protected @Nullable V value;

        public BasicEntry() {
        }

        public BasicEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            if (o instanceof Object2ObjectMap.Entry) {
                Entry<K, V> e = (Entry) o;
                return Objects.equals(this.key, e.getKey()) && Objects.equals(this.value, e.getValue());
            }
            Map.Entry<?, ?> e = (Map.Entry) o;
            Object key = e.getKey();
            Object value = e.getValue();
            return Objects.equals(this.key, key) && Objects.equals(this.value, value);
        }

        @Override
        public @Nullable K getKey() {
            return this.key;
        }

        @Override
        public @Nullable V getValue() {
            return this.value;
        }

        @Override
        public int hashCode() {
            //noinspection NonFinalFieldReferencedInHashCode
            return (this.key == null ? 0 : this.key.hashCode()) ^ (this.value == null ? 0 : this.value.hashCode());
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return this.key + "->" + this.value;
        }
    }

    private final class EntrySet extends AbstractObjectSet<Object2ObjectMap.Entry<K, V>> implements Object2ObjectMap.FastEntrySet<K, V> {

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry e)) {
                return false;
            }
            return O2OArrayMap.this.containsKey(e.getKey()) && Objects.equals(O2OArrayMap.this.get(e.getKey()), e.getValue());
        }

        @Override
        public void fastForEach(Consumer<? super Object2ObjectMap.Entry<K, V>> action) {
            BasicEntry<K, V> entry = new BasicEntry<>();
            int i = 0;
            for (int max = O2OArrayMap.this.size; i < max; ++i) {
                entry.key = O2OArrayMap.this.key[i];
                entry.value = O2OArrayMap.this.value[i];
                action.accept(entry);
            }
        }

        @Override
        public ObjectIterator<Object2ObjectMap.Entry<K, V>> fastIterator() {
            return new ObjectIterator<>() {
                int curr = -1;
                final BasicEntry<K, V> entry = new BasicEntry<>();
                int next;

                @Override
                public void forEachRemaining(Consumer<? super Object2ObjectMap.Entry<K, V>> action) {
                    int max = O2OArrayMap.this.size;
                    while (this.next < max) {
                        this.entry.key = O2OArrayMap.this.key[this.curr = this.next];
                        this.entry.value = O2OArrayMap.this.value[this.next++];
                        action.accept(this.entry);
                    }
                }

                @Override
                public boolean hasNext() {
                    return this.next < O2OArrayMap.this.size;
                }

                @Override
                public Object2ObjectMap.Entry<K, V> next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    this.entry.key = O2OArrayMap.this.key[this.curr = this.next];
                    this.entry.value = O2OArrayMap.this.value[this.next++];
                    return this.entry;
                }

                @Override
                public void remove() {
                    if (this.curr == -1) {
                        throw new IllegalStateException();
                    }
                    this.curr = -1;
                    int tail = O2OArrayMap.this.size-- - this.next--;
                    System.arraycopy(O2OArrayMap.this.key, this.next + 1, O2OArrayMap.this.key, this.next, tail);
                    System.arraycopy(O2OArrayMap.this.value, this.next + 1, O2OArrayMap.this.value, this.next, tail);
                    O2OArrayMap.this.key[O2OArrayMap.this.size] = null;
                    O2OArrayMap.this.value[O2OArrayMap.this.size] = null;
                }
            };
        }

        @Override
        public void forEach(Consumer<? super Object2ObjectMap.Entry<K, V>> action) {
            int i = 0;
            for (int max = O2OArrayMap.this.size; i < max; ++i) {
                //noinspection ObjectAllocationInLoop
                action.accept(new BasicEntry<>(O2OArrayMap.this.key[i], O2OArrayMap.this.value[i]));
            }
        }

        @Override
        public ObjectIterator<Object2ObjectMap.Entry<K, V>> iterator() {
            return new ObjectIterator<>() {
                int curr = -1;
                int next;

                @Override
                public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
                    int max = O2OArrayMap.this.size;
                    while (this.next < max) {
                        action.accept(new BasicEntry<>(O2OArrayMap.this.key[this.curr = this.next], O2OArrayMap.this.value[this.next++]));
                    }
                }

                @Override
                public boolean hasNext() {
                    return this.next < O2OArrayMap.this.size;
                }

                @Override
                public Object2ObjectMap.Entry<K, V> next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return new BasicEntry<>(O2OArrayMap.this.key[this.curr = this.next], O2OArrayMap.this.value[this.next++]);
                }

                @Override
                public void remove() {
                    if (this.curr == -1) {
                        throw new IllegalStateException();
                    }
                    this.curr = -1;
                    int tail = O2OArrayMap.this.size-- - this.next--;
                    System.arraycopy(O2OArrayMap.this.key, this.next + 1, O2OArrayMap.this.key, this.next, tail);
                    System.arraycopy(O2OArrayMap.this.value, this.next + 1, O2OArrayMap.this.value, this.next, tail);
                    O2OArrayMap.this.key[O2OArrayMap.this.size] = null;
                    O2OArrayMap.this.value[O2OArrayMap.this.size] = null;
                }
            };
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry) o;
            int oldPos = O2OArrayMap.this.findKey(e.getKey());
            if (oldPos != -1 && Objects.equals(e.getValue(), O2OArrayMap.this.value[oldPos])) {
                int tail = O2OArrayMap.this.size - oldPos - 1;
                System.arraycopy(O2OArrayMap.this.key, oldPos + 1, O2OArrayMap.this.key, oldPos, tail);
                System.arraycopy(O2OArrayMap.this.value, oldPos + 1, O2OArrayMap.this.value, oldPos, tail);
                O2OArrayMap.this.size--;
                O2OArrayMap.this.key[O2OArrayMap.this.size] = null;
                O2OArrayMap.this.value[O2OArrayMap.this.size] = null;
                return true;
            }
            return false;
        }

        @Override
        public int size() {
            return O2OArrayMap.this.size;
        }

        @Override
        public ObjectSpliterator<Object2ObjectMap.Entry<K, V>> spliterator() {
            return new EntrySetSpliterator(0, O2OArrayMap.this.size);
        }

        final class EntrySetSpliterator extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Object2ObjectMap.Entry<K, V>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.SUBSIZED;
            }

            @Override
            protected Object2ObjectMap.Entry<K, V> get(int location) {
                return new BasicEntry<>(O2OArrayMap.this.key[location], O2OArrayMap.this.value[location]);
            }

            @Override
            protected EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return EntrySet.this.new EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet extends AbstractObjectSet<K> {

        @Override
        public void clear() {
            O2OArrayMap.this.clear();
        }

        @Override
        public boolean contains(Object k) {
            return O2OArrayMap.this.findKey(k) != -1;
        }

        @Override
        public void forEach(Consumer<? super K> action) {
            int i = 0;
            for (int max = O2OArrayMap.this.size; i < max; ++i) {
                action.accept(O2OArrayMap.this.key[i]);
            }
        }

        @Override
        public ObjectIterator<K> iterator() {
            return new ObjectIterator<>() {
                int pos;

                @Override
                public void forEachRemaining(Consumer<? super K> action) {
                    int max = O2OArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(O2OArrayMap.this.key[this.pos++]);
                    }
                }

                @Override
                public boolean hasNext() {
                    return this.pos < O2OArrayMap.this.size;
                }

                @Override
                public K next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return O2OArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = O2OArrayMap.this.size - this.pos;
                    System.arraycopy(O2OArrayMap.this.key, this.pos, O2OArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(O2OArrayMap.this.value, this.pos, O2OArrayMap.this.value, this.pos - 1, tail);
                    O2OArrayMap.this.size--;
                    --this.pos;
                    O2OArrayMap.this.key[O2OArrayMap.this.size] = null;
                    O2OArrayMap.this.value[O2OArrayMap.this.size] = null;
                }
            };
        }

        @Override
        public boolean remove(Object k) {
            int oldPos = O2OArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = O2OArrayMap.this.size - oldPos - 1;
            System.arraycopy(O2OArrayMap.this.key, oldPos + 1, O2OArrayMap.this.key, oldPos, tail);
            System.arraycopy(O2OArrayMap.this.value, oldPos + 1, O2OArrayMap.this.value, oldPos, tail);
            O2OArrayMap.this.size--;
            O2OArrayMap.this.key[O2OArrayMap.this.size] = null;
            O2OArrayMap.this.value[O2OArrayMap.this.size] = null;
            return true;
        }

        @Override
        public int size() {
            return O2OArrayMap.this.size;
        }

        @Override
        public ObjectSpliterator<K> spliterator() {
            return new KeySetSpliterator(0, O2OArrayMap.this.size);
        }

        final class KeySetSpliterator extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<K> {
            KeySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.SUBSIZED;
            }

            @Override
            public void forEachRemaining(Consumer<? super K> action) {
                int max = O2OArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(O2OArrayMap.this.key[this.pos++]);
                }
            }

            @Override
            protected K get(int location) {
                return O2OArrayMap.this.key[location];
            }

            @Override
            protected KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return KeySet.this.new KeySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class ValuesCollection extends AbstractObjectCollection<V> {

        @Override
        public void clear() {
            O2OArrayMap.this.clear();
        }

        @Override
        public boolean contains(Object v) {
            return O2OArrayMap.this.containsValue(v);
        }

        @Override
        public void forEach(Consumer<? super V> action) {
            int i = 0;
            for (int max = O2OArrayMap.this.size; i < max; ++i) {
                action.accept(O2OArrayMap.this.value[i]);
            }
        }

        @Override
        public ObjectIterator<V> iterator() {
            return new ObjectIterator<>() {
                int pos;

                @Override
                public void forEachRemaining(Consumer<? super V> action) {
                    int max = O2OArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(O2OArrayMap.this.value[this.pos++]);
                    }
                }

                @Override
                public boolean hasNext() {
                    return this.pos < O2OArrayMap.this.size;
                }

                @Override
                public V next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return O2OArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = O2OArrayMap.this.size - this.pos;
                    System.arraycopy(O2OArrayMap.this.key, this.pos, O2OArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(O2OArrayMap.this.value, this.pos, O2OArrayMap.this.value, this.pos - 1, tail);
                    O2OArrayMap.this.size--;
                    --this.pos;
                    O2OArrayMap.this.key[O2OArrayMap.this.size] = null;
                    O2OArrayMap.this.value[O2OArrayMap.this.size] = null;
                }
            };
        }

        @Override
        public int size() {
            return O2OArrayMap.this.size;
        }

        @Override
        public ObjectSpliterator<V> spliterator() {
            return new ValuesSpliterator(0, O2OArrayMap.this.size);
        }

        final class ValuesSpliterator extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<V> {
            ValuesSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
            }

            @Override
            public void forEachRemaining(Consumer<? super V> action) {
                int max = O2OArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(O2OArrayMap.this.value[this.pos++]);
                }
            }

            @Override
            protected V get(int location) {
                return O2OArrayMap.this.value[location];
            }

            @Override
            protected ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return ValuesCollection.this.new ValuesSpliterator(pos, maxPos);
            }
        }
    }
}
