package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

public class O2OArrayMap<K, V> extends AbstractObject2ObjectMap<K, V> implements O2OMap<K, V> {

    protected final O2OMap.Entry<K, V> entry = new O2OMap.Entry<>();
    /**
     * The keys (valid up to {@link #size}, excluded).
     */
    protected Object[] key;
    protected int lastPos = -1;
    /**
     * The number of valid entries in {@link #key} and {@link #value}.
     */
    protected int size;
    /**
     * The values (parallel to {@link #key}).
     */
    protected transient Object[] value;
    /**
     * Cached set of entries.
     */
    private @Nullable FastEntrySet<K, V> entries;
    /**
     * Cached set of keys.
     */
    private @Nullable ObjectSet<K> keys;
    /**
     * Cached collection of values.
     */
    private transient @Nullable ObjectCollection<V> values;

    /**
     * Creates a new empty array map with given key and value backing arrays. The
     * resulting map will have as many entries as the given arrays.
     *
     * <p>
     * It is responsibility of the caller that the elements of {@code key} are
     * distinct.
     *
     * @param key   the key array.
     * @param value the value array (it <em>must</em> have the same length as
     *              {@code key}).
     */
    public O2OArrayMap(final Object[] key, final Object[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException(
                    "Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    /**
     * Creates a new empty array map.
     */
    public O2OArrayMap() {
        this.key = ObjectArrays.EMPTY_ARRAY;
        this.value = ObjectArrays.EMPTY_ARRAY;
    }

    /**
     * Creates a new empty array map of given capacity.
     *
     * @param capacity the initial capacity.
     */
    public O2OArrayMap(final int capacity) {
        this.key = new Object[capacity];
        this.value = new Object[capacity];
    }

    /**
     * Creates a new empty array map copying the entries of a given map.
     *
     * @param m a map.
     */
    public O2OArrayMap(final O2OMap<K, V> m) {
        this(m.size());
        int i = 0;
        for (O2OMap.Entry<K, V> e = m.fastEntries(); e != null; e = m.fastEntries(), ++i) {
            this.key[i] = e.key();
            this.value[i] = e.value();
        }
        this.size = i;
    }

    /**
     * Creates a new empty array map copying the entries of a given map.
     *
     * @param m a map.
     */
    public O2OArrayMap(final Map<? extends K, ? extends V> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            i++;
        }
        this.size = i;
    }

    /**
     * Creates a new array map with given key and value backing arrays, using the
     * given number of elements.
     *
     * <p>
     * It is responsibility of the caller that the first {@code size} elements of
     * {@code key} are distinct.
     *
     * @param key   the key array.
     * @param value the value array (it <em>must</em> have the same length as
     *              {@code key}).
     * @param size  the number of valid elements in {@code key} and {@code value}.
     */
    public O2OArrayMap(final Object[] key, final Object[] value, final int size) {
        this.key = key;
        this.value = value;
        this.size = size;
        if (key.length != value.length) {
            throw new IllegalArgumentException(
                    "Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
        if (size > key.length) {
            throw new IllegalArgumentException("The provided size (" + size
                                               + ") is larger than or equal to the backing-arrays size (" + key.length + ")");
        }
    }

    @Override
    public void clear() {
        for (int i = this.size; i-- != 0; ) {
            this.key[i] = null;
            this.value[i] = null;
        }
        this.size = 0;
    }

    /**
     * Returns a deep copy of this map.
     *
     * <p>
     * This method performs a deep copy of this hash map; the data stored in the
     * map, however, is not cloned. Note that this makes a difference only for
     * object keys.
     *
     * @return a deep copy of this map.
     */
    @Override
    public O2OArrayMap<K, V> clone() {
        O2OArrayMap<K, V> c;
        try {
            c = (O2OArrayMap<K, V>) super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = this.key.clone();
        c.value = this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    @Override
    public boolean containsKey(final Object k) {
        return this.findKey(k) != -1;
    }

    @Override
    public boolean containsValue(Object v) {
        for (int i = this.size; i-- != 0; ) {
            if (Objects.equals(this.value[i], v)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public O2OMap.@Nullable Entry<K, V> fastEntries() {
        if (this.isEmpty()) {
            this.lastPos = -1;
            return null;
        }
        if (++this.lastPos < this.size) {
            return this.entry.set((K) this.key[this.lastPos], (V) this.value[this.lastPos]);
        }
        this.lastPos = -1;
        this.entry.set(null, null);
        return null;
    }

    private int findKey(final Object k) {
        final Object[] key = this.key;
        for (int i = this.size; i-- != 0; ) {
            if (Objects.equals(key[i], k)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public @Nullable V get(final Object k) {
        final Object[] key = this.key;
        for (int i = this.size; i-- != 0; ) {
            if (Objects.equals(key[i], k)) {
                return (V) this.value[i];
            }
        }
        return this.defRetValue;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public ObjectSet<K> keySet() {
        if (this.keys == null) {
            this.keys = new O2OArrayMap.KeySet();
        }
        return this.keys;
    }

    @Override
    public FastEntrySet<K, V> object2ObjectEntrySet() {
        if (this.entries == null) {
            this.entries = new O2OArrayMap.EntrySet();
        }
        return this.entries;
    }

    @Override
    public V put(K k, V v) {
        final int oldKey = this.findKey(k);
        if (oldKey != -1) {
            final V oldValue = (V) this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            final Object[] newKey = new Object[this.size == 0 ? 2 : this.size * 2];
            final Object[] newValue = new Object[this.size == 0 ? 2 : this.size * 2];
            for (int i = this.size; i-- != 0; ) {
                newKey[i] = this.key[i];
                newValue[i] = this.value[i];
            }
            this.key = newKey;
            this.value = newValue;
        }
        this.key[this.size] = k;
        this.value[this.size] = v;
        this.size++;
        return this.defRetValue;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new Object[this.size];
        this.value = new Object[this.size];
        for (int i = 0; i < this.size; i++) {
            this.key[i] = s.readObject();
            this.value[i] = s.readObject();
        }
    }

    @Override
    public V remove(final Object k) {
        final int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        if (oldPos == this.lastPos) {
            --this.lastPos;
        }
        final V oldValue = (V) this.value[oldPos];
        final int tail = this.size - oldPos - 1;
        System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
        System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
        this.size--;
        this.key[this.size] = null;
        this.value[this.size] = null;
        return oldValue;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void trimCollection() {
        if (this.size == 0) {
            this.key = ObjectArrays.EMPTY_ARRAY;
            this.value = ObjectArrays.EMPTY_ARRAY;
        }
        else if (this.size != this.key.length) {
            Object[] newKey = new Object[this.size];
            System.arraycopy(this.key, 0, newKey, 0, this.size);
            Object[] newValue = new Object[this.size];
            System.arraycopy(this.value, 0, newValue, 0, this.size);
            this.key = newKey;
            this.value = newValue;
        }
    }

    @Override
    public ObjectCollection<V> values() {
        if (this.values == null) {
            this.values = new O2OArrayMap.ValuesCollection();
        }
        return this.values;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        for (int i = 0, max = this.size; i < max; i++) {
            s.writeObject(this.key[i]);
            s.writeObject(this.value[i]);
        }
    }

    private final class EntrySet extends AbstractObjectSet<Object2ObjectMap.Entry<K, V>> implements FastEntrySet<K, V> {
        @Override
        public boolean contains(Object o) {
            if (!(o instanceof final Map.Entry<?, ?> e)) {
                return false;
            }
            final K k = (K) e.getKey();
            return O2OArrayMap.this.containsKey(k) && Objects.equals(O2OArrayMap.this.get(k), e.getValue());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void fastForEach(final Consumer<? super Object2ObjectMap.Entry<K, V>> action) {
            final O2OMap.BasicEntry<K, V> entry = new O2OMap.BasicEntry<>();
            // Hoist containing class field ref into local
            for (int i = 0, max = O2OArrayMap.this.size; i < max; ++i) {
                entry.key = (K) O2OArrayMap.this.key[i];
                entry.value = (V) O2OArrayMap.this.value[i];
                action.accept(entry);
            }
        }

        @Override
        public ObjectIterator<Object2ObjectMap.Entry<K, V>> fastIterator() {
            return new ObjectIterator<>() {
                final O2OMap.BasicEntry<K, V> entry = new O2OMap.BasicEntry<>();
                int curr = -1;
                int next;

                @Override
                public void forEachRemaining(final Consumer<? super Object2ObjectMap.Entry<K, V>> action) {
                    // Hoist containing class field ref into local
                    final int max = O2OArrayMap.this.size;
                    while (this.next < max) {
                        this.entry.key = (K) O2OArrayMap.this.key[this.curr = this.next];
                        this.entry.value = (V) O2OArrayMap.this.value[this.next++];
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
                    this.entry.key = (K) O2OArrayMap.this.key[this.curr = this.next];
                    this.entry.value = (V) O2OArrayMap.this.value[this.next++];
                    return this.entry;
                }

                @Override
                public void remove() {
                    if (this.curr == -1) {
                        throw new IllegalStateException();
                    }
                    this.curr = -1;
                    final int tail = O2OArrayMap.this.size-- - this.next--;
                    System.arraycopy(O2OArrayMap.this.key, this.next + 1, O2OArrayMap.this.key, this.next, tail);
                    System.arraycopy(O2OArrayMap.this.value, this.next + 1, O2OArrayMap.this.value, this.next, tail);
                    O2OArrayMap.this.key[O2OArrayMap.this.size] = null;
                    O2OArrayMap.this.value[O2OArrayMap.this.size] = null;
                }
            };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void forEach(final Consumer<? super Object2ObjectMap.Entry<K, V>> action) {
            // Hoist containing class field ref into local
            for (int i = 0, max = O2OArrayMap.this.size; i < max; ++i) {
                //noinspection ObjectAllocationInLoop
                action.accept(new O2OMap.BasicEntry<>((K) O2OArrayMap.this.key[i], (V) O2OArrayMap.this.value[i]));
            }
        }

        // (same for other collection view types)
        @Override
        public ObjectIterator<Object2ObjectMap.Entry<K, V>> iterator() {
            return new ObjectIterator<>() {
                int curr = -1;
                int next;

                @Override
                public void forEachRemaining(final Consumer<? super Object2ObjectMap.Entry<K, V>> action) {
                    // Hoist containing class field ref into local
                    final int max = O2OArrayMap.this.size;
                    while (this.next < max) {
                        action.accept(
                                new AbstractObject2ObjectMap.BasicEntry<>((K) O2OArrayMap.this.key[this.curr = this.next],
                                                                          (V) O2OArrayMap.this.value[this.next++]));
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
                    return new AbstractObject2ObjectMap.BasicEntry<>((K) O2OArrayMap.this.key[this.curr = this.next],
                                                                     (V) O2OArrayMap.this.value[this.next++]);
                }

                @Override
                public void remove() {
                    if (this.curr == -1) {
                        throw new IllegalStateException();
                    }
                    this.curr = -1;
                    final int tail = O2OArrayMap.this.size-- - this.next--;
                    System.arraycopy(O2OArrayMap.this.key, this.next + 1, O2OArrayMap.this.key, this.next, tail);
                    System.arraycopy(O2OArrayMap.this.value, this.next + 1, O2OArrayMap.this.value, this.next, tail);
                    O2OArrayMap.this.key[O2OArrayMap.this.size] = null;
                    O2OArrayMap.this.value[O2OArrayMap.this.size] = null;
                }
            };
        }

        @Override
        public boolean remove(final Object o) {
            if (!(o instanceof final Map.Entry<?, ?> e)) {
                return false;
            }
            final K k = (K) e.getKey();
            final V v = (V) e.getValue();
            final int oldPos = O2OArrayMap.this.findKey(k);
            if (oldPos == -1 || !Objects.equals(v, O2OArrayMap.this.value[oldPos])) {
                return false;
            }
            final int tail = O2OArrayMap.this.size - oldPos - 1;
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
        public ObjectSpliterator<Object2ObjectMap.Entry<K, V>> spliterator() {
            return new O2OArrayMap.EntrySet.EntrySetSpliterator(0, O2OArrayMap.this.size);
        }

        // We already have to create an Entry object for each iteration, so the overhead
        // from having
        // skeletal implementations isn't significant.
        final class EntrySetSpliterator extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Object2ObjectMap.Entry<K, V>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return ObjectSpliterators.SET_SPLITERATOR_CHARACTERISTICS | Spliterator.SUBSIZED | Spliterator.ORDERED;
            }

            @Override
            protected Object2ObjectMap.Entry<K, V> get(int location) {
                return new AbstractObject2ObjectMap.BasicEntry<>((K) O2OArrayMap.this.key[location], (V) O2OArrayMap.this.value[location]);
            }

            @Override
            protected O2OArrayMap.EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new O2OArrayMap.EntrySet.EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet extends AbstractObjectSet<K> {
        @Override
        public void clear() {
            O2OArrayMap.this.clear();
        }

        @Override
        public boolean contains(final Object k) {
            return O2OArrayMap.this.findKey(k) != -1;
        }

        @Override
        public void forEach(Consumer<? super K> action) {
            // Hoist containing class field ref into local
            for (int i = 0, max = O2OArrayMap.this.size; i < max; ++i) {
                action.accept((K) O2OArrayMap.this.key[i]);
            }
        }

        @Override
        public ObjectIterator<K> iterator() {
            return new ObjectIterator<>() {
                int pos;

                @Override
                public void forEachRemaining(final Consumer<? super K> action) {
                    // Hoist containing class field ref into local
                    final int max = O2OArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept((K) O2OArrayMap.this.key[this.pos++]);
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
                    return (K) O2OArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    final int tail = O2OArrayMap.this.size - this.pos;
                    System.arraycopy(O2OArrayMap.this.key, this.pos, O2OArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(O2OArrayMap.this.value, this.pos, O2OArrayMap.this.value, this.pos - 1, tail);
                    O2OArrayMap.this.size--;
                    this.pos--;
                    O2OArrayMap.this.key[O2OArrayMap.this.size] = null;
                    O2OArrayMap.this.value[O2OArrayMap.this.size] = null;
                }
            };
        }

        @Override
        public boolean remove(final Object k) {
            final int oldPos = O2OArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            final int tail = O2OArrayMap.this.size - oldPos - 1;
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
            return new O2OArrayMap.KeySet.KeySetSpliterator(0, O2OArrayMap.this.size);
        }

        final class KeySetSpliterator extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<K> {
            KeySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return ObjectSpliterators.SET_SPLITERATOR_CHARACTERISTICS | java.util.Spliterator.SUBSIZED
                       | java.util.Spliterator.ORDERED;
            }

            @Override
            public void forEachRemaining(final Consumer<? super K> action) {
                // Hoist containing class field ref into local
                final int max = O2OArrayMap.this.size;
                while (this.pos < max) {
                    action.accept((K) O2OArrayMap.this.key[this.pos++]);
                }
            }

            @Override
            protected K get(int location) {
                return (K) O2OArrayMap.this.key[location];
            }

            @Override
            protected O2OArrayMap.KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new O2OArrayMap.KeySet.KeySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class ValuesCollection extends AbstractObjectCollection<V> {
        @Override
        public void clear() {
            O2OArrayMap.this.clear();
        }

        @Override
        public boolean contains(final Object v) {
            return O2OArrayMap.this.containsValue(v);
        }

        @Override
        public void forEach(Consumer<? super V> action) {
            // Hoist containing class field ref into local
            for (int i = 0, max = O2OArrayMap.this.size; i < max; ++i) {
                action.accept((V) O2OArrayMap.this.value[i]);
            }
        }

        @Override
        public ObjectIterator<V> iterator() {
            return new ObjectIterator<>() {
                int pos;

                @Override
                public void forEachRemaining(final Consumer<? super V> action) {
                    // Hoist containing class field ref into local
                    final int max = O2OArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept((V) O2OArrayMap.this.value[this.pos++]);
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
                    return (V) O2OArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    final int tail = O2OArrayMap.this.size - this.pos;
                    System.arraycopy(O2OArrayMap.this.key, this.pos, O2OArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(O2OArrayMap.this.value, this.pos, O2OArrayMap.this.value, this.pos - 1, tail);
                    O2OArrayMap.this.size--;
                    this.pos--;
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
            return new O2OArrayMap.ValuesCollection.ValuesSpliterator(0, O2OArrayMap.this.size);
        }

        final class ValuesSpliterator extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<V> {

            ValuesSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return ObjectSpliterators.COLLECTION_SPLITERATOR_CHARACTERISTICS | Spliterator.SUBSIZED | Spliterator.ORDERED;
            }

            @Override
            public void forEachRemaining(final Consumer<? super V> action) {
                // Hoist containing class field ref into local
                final int max = O2OArrayMap.this.size;
                while (this.pos < max) {
                    action.accept((V) O2OArrayMap.this.value[this.pos++]);
                }
            }

            @Override
            protected V get(int location) {
                return (V) O2OArrayMap.this.value[location];
            }

            @Override
            protected O2OArrayMap.ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new O2OArrayMap.ValuesCollection.ValuesSpliterator(pos, maxPos);
            }
        }
    }
}
