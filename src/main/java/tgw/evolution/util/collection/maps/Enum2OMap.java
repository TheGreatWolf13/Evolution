package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.ArrayHelper;

import java.util.*;
import java.util.function.Consumer;

public class Enum2OMap<K extends Enum<K>, V> extends AbstractReference2ObjectMap<K, V> implements R2OMap<K, V> {

    private final Class<K> clazz;
    private boolean containsNullKey;
    private @Nullable FastEntrySet<K, V> entrySet;
    private final K[] key;
    private int size;
    private final V[] value;
    private @Nullable R2OMap<K, V> view;

    public Enum2OMap(Class<K> clazz) {
        if (!clazz.isEnum()) {
            throw new IllegalStateException("Class is not an enum!");
        }
        this.clazz = clazz;
        K[] array = ArrayHelper.getOrCacheArray(clazz);
        this.key = (K[]) new Enum[array.length];
        this.value = (V[]) new Object[array.length + 1];
    }

    @Override
    public long beginIteration() {
        if (this.isEmpty()) {
            return 0;
        }
        if (this.containsNullKey) {
            return -1L << 32 | this.size;
        }
        for (int i = 0, len = this.key.length; i < len; i++) {
            if (this.key[i] != null) {
                return (long) i << 32 | this.size;
            }
        }
        throw new IllegalStateException("Should never reach here!");
    }

    @Override
    public void clear() {
        Arrays.fill(this.key, null);
        Arrays.fill(this.value, null);
        this.containsNullKey = false;
        this.size = 0;
    }

    @Override
    public boolean containsKey(@Nullable Object k) {
        if (k == null) {
            return this.containsNullKey;
        }
        return this.isValidKey(k) && this.value[this.clazz.cast(k).ordinal()] != null;
    }

    @Override
    public boolean containsValue(@Nullable Object v) {
        if (this.containsNullKey) {
            if (Objects.equals(this.value[this.value.length - 1], v)) {
                return true;
            }
        }
        for (int i = 0, len = this.key.length; i < len; i++) {
            if (this.key[i] != null) {
                if (Objects.equals(this.value[i], v)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable V get(@Nullable Object key) {
        if (key == null) {
            if (this.containsNullKey) {
                return this.value[this.value.length - 1];
            }
            return null;
        }
        return this.isValidKey(key) ? this.value[this.clazz.cast(key).ordinal()] : null;
    }

    @Override
    public @Nullable K getIterationKey(long it) {
        int pos = (int) (it >> 32);
        if (pos == -1) {
            return null;
        }
        return this.key[pos];
    }

    @Override
    public V getIterationValue(long it) {
        int pos = (int) (it >> 32);
        if (pos == -1) {
            return this.value[this.value.length - 1];
        }
        return this.value[pos];
    }

    @Override
    public @Nullable K getSampleKey() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Empty map!");
        }
        if (this.containsNullKey) {
            return null;
        }
        for (K k : this.key) {
            if (k != null) {
                return k;
            }
        }
        throw new IllegalStateException("Should never reach here!");
    }

    @Override
    public V getSampleValue() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Empty map!");
        }
        if (this.containsNullKey) {
            return this.value[this.value.length - 1];
        }
        for (int i = 0, len = this.key.length; i < len; i++) {
            if (this.key[i] != null) {
                return this.value[i];
            }
        }
        throw new IllegalStateException("Should never reach here!");
    }

    @Override
    public ReferenceSet<K> keySet() {
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
        if (pos == -1) {
            pos = 0;
        }
        else {
            ++pos;
        }
        for (int i = pos, len = this.key.length; i < len; ++i) {
            if (this.key[i] != null) {
                return (long) i << 32 | size;
            }
        }
        throw new IllegalStateException("Should never reach here!");
    }

    @Override
    public V put(@Nullable K key, @Nullable V value) {
        if (key == null) {
            int pos = this.value.length - 1;
            V old = this.value[pos];
            if (!this.containsNullKey) {
                ++this.size;
            }
            this.containsNullKey = true;
            this.value[pos] = value;
            return old;
        }
        int index = key.ordinal();
        K k = this.key[index];
        this.key[index] = key;
        V old = this.value[index];
        this.value[index] = value;
        if (k == null) {
            ++this.size;
        }
        return old;
    }

    @Override
    public FastEntrySet<K, V> reference2ObjectEntrySet() {
        this.deprecatedMethod();
        if (this.entrySet == null) {
            this.entrySet = new EntrySet();
        }
        return this.entrySet;
    }

    @Override
    public @Nullable V remove(@Nullable Object key) {
        if (key == null) {
            if (this.containsNullKey) {
                --this.size;
                int pos = this.value.length - 1;
                V old = this.value[pos];
                this.value[pos] = null;
                return old;
            }
            return null;
        }
        if (!this.isValidKey(key)) {
            return null;
        }
        int index = this.clazz.cast(key).ordinal();
        K k = this.key[index];
        if (k == null) {
            return null;
        }
        --this.size;
        this.key[index] = null;
        V oldValue = this.value[index];
        this.value[index] = null;
        return oldValue;
    }

    @Override
    public long removeIteration(long it) {
        int pos = (int) (it >> 32);
        if (pos == -1) {
            this.containsNullKey = false;
            this.value[this.value.length - 1] = null;
            --this.size;
            return it;
        }
        this.key[pos] = null;
        this.value[pos] = null;
        --this.size;
        return it;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean trim() {
        return false;
    }

    @Override
    public ObjectCollection<V> values() {
        this.deprecatedMethod();
        return super.values();
    }

    @Override
    public @UnmodifiableView R2OMap<K, V> view() {
        if (this.view == null) {
            this.view = new View<>(this);
        }
        return this.view;
    }

    private boolean isValidKey(Object key) {
        Class<?> clazz = key.getClass();
        return clazz == this.clazz || clazz.getSuperclass() == this.clazz;
    }

    static class BasicEntry<K extends Enum<K>, V> implements Reference2ObjectMap.Entry<K, V> {

        private @Nullable K k;
        private final Enum2OMap<K, V> map;
        private V v;

        public BasicEntry(Enum2OMap<K, V> map, @Nullable K k, V v) {
            this.map = map;
            this.k = k;
            this.v = v;
        }

        public BasicEntry(Enum2OMap<K, V> map) {
            this.map = map;
        }

        @Override
        public @Nullable K getKey() {
            return this.k;
        }

        @Override
        public V getValue() {
            return this.v;
        }

        @Override
        public V setValue(V value) {
            if (this.k == null) {
                int pos = this.map.value.length - 1;
                V old = this.map.value[pos];
                this.map.value[pos] = value;
                return old;
            }
            int pos = this.k.ordinal();
            V old = this.map.value[pos];
            this.map.value[pos] = value;
            return old;
        }
    }

    private final class EntrySet extends AbstractObjectSet<Reference2ObjectMap.Entry<K, V>> implements FastEntrySet<K, V> {

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof final Map.Entry<?, ?> e)) {
                return false;
            }
            final K k = (K) e.getKey();
            return Enum2OMap.this.containsKey(k) && Objects.equals(Enum2OMap.this.get(k), e.getValue());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void fastForEach(final Consumer<? super Reference2ObjectMap.Entry<K, V>> action) {
            final BasicEntry<K, V> entry = new BasicEntry<>(Enum2OMap.this);
            if (Enum2OMap.this.containsNullKey) {
                entry.k = null;
                entry.v = Enum2OMap.this.value[Enum2OMap.this.value.length - 1];
                action.accept(entry);
            }
            for (int i = 0, len = Enum2OMap.this.key.length; i < len; ++i) {
                K k = Enum2OMap.this.key[i];
                if (k != null) {
                    entry.k = k;
                    entry.v = Enum2OMap.this.value[i];
                    action.accept(entry);
                }
            }
        }

        @Override
        public ObjectIterator<Reference2ObjectMap.Entry<K, V>> fastIterator() {
            return new ObjectIterator<>() {
                int count;
                int curr = -1;
                final BasicEntry<K, V> entry = new BasicEntry<>(Enum2OMap.this);
                int next;
                boolean shouldReturnNullKey = Enum2OMap.this.containsNullKey;

                @Override
                public void forEachRemaining(final Consumer<? super Reference2ObjectMap.Entry<K, V>> action) {
                    if (this.shouldReturnNullKey) {
                        this.entry.k = null;
                        this.entry.v = Enum2OMap.this.value[Enum2OMap.this.value.length - 1];
                        action.accept(this.entry);
                        this.shouldReturnNullKey = false;
                        this.curr = Enum2OMap.this.value.length - 1;
                        ++this.count;
                    }
                    while (this.hasNext()) {
                        K k = Enum2OMap.this.key[this.next];
                        if (k != null) {
                            this.entry.k = k;
                            this.entry.v = Enum2OMap.this.value[this.curr = this.next];
                            action.accept(this.entry);
                            ++this.count;
                        }
                        ++this.next;
                    }
                }

                @Override
                public boolean hasNext() {
                    return this.count < Enum2OMap.this.size;
                }

                @Override
                public Reference2ObjectMap.Entry<K, V> next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    if (this.shouldReturnNullKey) {
                        this.entry.k = null;
                        this.entry.v = Enum2OMap.this.value[Enum2OMap.this.value.length - 1];
                        this.shouldReturnNullKey = false;
                        this.curr = Enum2OMap.this.value.length - 1;
                        ++this.count;
                        return this.entry;
                    }
                    K k = Enum2OMap.this.key[this.next];
                    while (k == null) {
                        k = Enum2OMap.this.key[++this.next];
                    }
                    this.entry.k = k;
                    this.entry.v = Enum2OMap.this.value[this.curr = this.next++];
                    ++this.count;
                    return this.entry;
                }

                @Override
                public void remove() {
                    if (this.curr == -1) {
                        throw new IllegalStateException();
                    }
                    Enum2OMap.this.value[this.curr] = null;
                    --Enum2OMap.this.size;
                    --this.count;
                    this.curr = -1;
                }
            };
        }

        @Override
        public void forEach(final Consumer<? super Reference2ObjectMap.Entry<K, V>> action) {
            if (Enum2OMap.this.containsNullKey) {
                action.accept(new BasicEntry<>(Enum2OMap.this, null, Enum2OMap.this.value[Enum2OMap.this.value.length - 1]));
            }
            for (int i = 0, len = Enum2OMap.this.key.length; i < len; ++i) {
                K k = Enum2OMap.this.key[i];
                if (k != null) {
                    //noinspection ObjectAllocationInLoop
                    action.accept(new BasicEntry<>(Enum2OMap.this, k, Enum2OMap.this.value[i]));
                }
            }
        }

        @Override
        public ObjectIterator<Reference2ObjectMap.Entry<K, V>> iterator() {
            return new ObjectIterator<>() {
                int count;
                int curr = -1;
                int next;
                boolean shouldReturnNullKey = Enum2OMap.this.containsNullKey;

                @Override
                public void forEachRemaining(final Consumer<? super Reference2ObjectMap.Entry<K, V>> action) {
                    if (this.shouldReturnNullKey) {
                        int pos = Enum2OMap.this.value.length - 1;
                        action.accept(new BasicEntry<>(Enum2OMap.this, null, Enum2OMap.this.value[pos]));
                        this.curr = pos;
                        this.shouldReturnNullKey = false;
                        ++this.count;
                    }
                    while (this.hasNext()) {
                        K k = Enum2OMap.this.key[this.next];
                        if (k != null) {
                            action.accept(new BasicEntry<>(Enum2OMap.this, k, Enum2OMap.this.value[this.curr = this.next]));
                            ++this.count;
                        }
                        ++this.next;
                    }
                }

                @Override
                public boolean hasNext() {
                    return this.count < Enum2OMap.this.size;
                }

                @Override
                public Reference2ObjectMap.Entry<K, V> next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    if (this.shouldReturnNullKey) {
                        this.shouldReturnNullKey = false;
                        ++this.count;
                        return new BasicEntry<>(Enum2OMap.this, null, Enum2OMap.this.value[this.curr = Enum2OMap.this.value.length - 1]);
                    }
                    K k = Enum2OMap.this.key[this.next];
                    while (k == null) {
                        k = Enum2OMap.this.key[++this.next];
                    }
                    ++this.count;
                    return new BasicEntry<>(Enum2OMap.this, k, Enum2OMap.this.value[this.curr = this.next++]);
                }

                @Override
                public void remove() {
                    if (this.curr == -1) {
                        throw new IllegalStateException();
                    }
                    Enum2OMap.this.value[this.curr] = null;
                    --Enum2OMap.this.size;
                    --this.count;
                    this.curr = -1;
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
            if (k == null) {
                if (Enum2OMap.this.containsNullKey) {
                    int pos = Enum2OMap.this.value.length - 1;
                    if (Objects.equals(v, Enum2OMap.this.value[pos])) {
                        Enum2OMap.this.containsNullKey = false;
                        Enum2OMap.this.value[pos] = null;
                        --Enum2OMap.this.size;
                        return true;
                    }
                    return false;
                }
                return false;
            }
            final int oldPos = k.ordinal();
            K oldK = Enum2OMap.this.key[oldPos];
            if (oldK == null) {
                return false;
            }
            if (!Objects.equals(v, Enum2OMap.this.value[oldPos])) {
                return false;
            }
            Enum2OMap.this.key[oldPos] = null;
            Enum2OMap.this.value[oldPos] = null;
            --Enum2OMap.this.size;
            return true;
        }

        @Override
        public int size() {
            return Enum2OMap.this.size;
        }

        @Override
        public ObjectSpliterator<Reference2ObjectMap.Entry<K, V>> spliterator() {
            return new EntrySetSpliterator(0, Enum2OMap.this.size);
        }

        final class EntrySetSpliterator extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Reference2ObjectMap.Entry<K, V>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.SUBSIZED;
            }

            @Override
            protected Reference2ObjectMap.Entry<K, V> get(int location) {
                return new BasicEntry<>(Enum2OMap.this, Enum2OMap.this.key[location], Enum2OMap.this.value[location]);
            }

            @Override
            protected EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySet.EntrySetSpliterator(pos, maxPos);
            }
        }
    }
}
