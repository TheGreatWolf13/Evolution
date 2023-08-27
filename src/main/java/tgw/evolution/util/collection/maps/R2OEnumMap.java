package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Consumer;

public class R2OEnumMap<K extends Enum<K>, V> extends AbstractReference2ObjectMap<K, V> implements R2OMap<K, V> {

    private final R2OMap.Entry entry = new R2OMap.Entry();
    private final K[] key;
    private final V[] value;
    protected int lastPos = -1;
    private @Nullable FastEntrySet<K, V> entrySet;
    private int size;
    private @Nullable R2OMap<K, V> view;

    public R2OEnumMap(K[] k) {
        this.key = k;
        this.value = (V[]) new Object[k.length];
    }

    @Override
    public void clear() {
        Arrays.fill(this.value, null);
        this.size = 0;
    }

    @Override
    public boolean containsKey(Object k) {
        return this.isValidKey(k) && this.value[((Enum<?>) k).ordinal()] != null;
    }

    @Override
    public boolean containsValue(Object v) {
        if (v == null) {
            return false;
        }
        for (V v1 : this.value) {
            if (v.equals(v1)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public R2OMap.@Nullable Entry<K, V> fastEntries() {
        if (this.isEmpty()) {
            this.lastPos = -1;
            return null;
        }
        int len = this.value.length;
        while (++this.lastPos < len) {
            V v = this.value[this.lastPos];
            if (v != null) {
                return this.entry.set(this.key[this.lastPos], v);
            }
        }
        this.lastPos = -1;
        this.entry.set(null, null);
        return null;
    }

    @Override
    public @Nullable V get(Object key) {
        return this.isValidKey(key) ? this.value[((Enum<?>) key).ordinal()] : null;
    }

    @Contract("null -> false")
    private boolean isValidKey(@Nullable Object key) {
        if (key == null) {
            return false;
        }
        return key.getClass() == this.key[0].getClass();
    }

    @Override
    public V put(K key, V value) {
        int index = key.ordinal();
        V old = this.value[index];
        this.value[index] = value;
        if (old == null) {
            ++this.size;
        }
        return old;
    }

    @Override
    public FastEntrySet<K, V> reference2ObjectEntrySet() {
        if (this.entrySet == null) {
            this.entrySet = new EntrySet();
        }
        return this.entrySet;
    }

    @Override
    public @Nullable V remove(Object key) {
        if (!this.isValidKey(key)) {
            return null;
        }
        int index = ((Enum<?>) key).ordinal();
        V oldValue = this.value[index];
        this.value[index] = null;
        //noinspection VariableNotUsedInsideIf
        if (oldValue != null) {
            --this.size;
        }
        return oldValue;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void trimCollection() {
    }

    @Override
    public @UnmodifiableView R2OMap<K, V> view() {
        if (this.view == null) {
            this.view = new View<>(this);
        }
        return this.view;
    }

    private final class EntrySet extends AbstractObjectSet<Reference2ObjectMap.Entry<K, V>> implements FastEntrySet<K, V> {

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof final Map.Entry<?, ?> e)) {
                return false;
            }
            final K k = (K) e.getKey();
            return R2OEnumMap.this.containsKey(k) && Objects.equals(R2OEnumMap.this.get(k), e.getValue());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void fastForEach(final Consumer<? super Reference2ObjectMap.Entry<K, V>> action) {
            final R2OMap.BasicEntry<K, V> entry = new R2OMap.BasicEntry<>();
            // Hoist containing class field ref into local
            for (int i = 0, max = R2OEnumMap.this.size; i < max; ++i) {
                entry.key = R2OEnumMap.this.key[i];
                entry.value = R2OEnumMap.this.value[i];
                action.accept(entry);
            }
        }

        @Override
        public ObjectIterator<Reference2ObjectMap.Entry<K, V>> fastIterator() {
            return new ObjectIterator<>() {
                final R2OMap.BasicEntry<K, V> entry = new R2OMap.BasicEntry<>();
                int count;
                int curr = -1;
                int next;

                @Override
                public void forEachRemaining(final Consumer<? super Reference2ObjectMap.Entry<K, V>> action) {
                    // Hoist containing class field ref into local
                    final int max = R2OEnumMap.this.size;
                    while (this.next < max) {
                        V v = R2OEnumMap.this.value[this.next];
                        if (v != null) {
                            this.entry.key = R2OEnumMap.this.key[this.curr = this.next];
                            this.entry.value = v;
                            action.accept(this.entry);
                        }
                        ++this.next;
                    }
                    this.count = max;
                }

                @Override
                public boolean hasNext() {
                    return this.count < R2OEnumMap.this.size;
                }

                @Override
                public Reference2ObjectMap.Entry<K, V> next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    V v = R2OEnumMap.this.value[this.next];
                    while (v == null) {
                        v = R2OEnumMap.this.value[++this.next];
                    }
                    this.entry.key = R2OEnumMap.this.key[this.curr = this.next++];
                    this.entry.value = v;
                    ++this.count;
                    return this.entry;
                }

                @Override
                public void remove() {
                    if (this.curr == -1) {
                        throw new IllegalStateException();
                    }
                    R2OEnumMap.this.value[this.curr] = null;
                    --R2OEnumMap.this.size;
                    --this.count;
                    this.curr = -1;
                }
            };
        }

        @Override
        public void forEach(final Consumer<? super Reference2ObjectMap.Entry<K, V>> action) {
            for (int i = 0, max = R2OEnumMap.this.size; i < max; ++i) {
                V v = R2OEnumMap.this.value[i];
                if (v != null) {
                    //noinspection ObjectAllocationInLoop
                    action.accept(new R2OMap.BasicEntry<>(R2OEnumMap.this.key[i], v));
                }
            }
        }

        @Override
        public ObjectIterator<Reference2ObjectMap.Entry<K, V>> iterator() {
            return new ObjectIterator<>() {
                int count;
                int curr = -1;
                int next;

                @Override
                public void forEachRemaining(final Consumer<? super Reference2ObjectMap.Entry<K, V>> action) {
                    final int max = R2OEnumMap.this.size;
                    while (this.next < max) {
                        V v = R2OEnumMap.this.value[this.next];
                        if (v != null) {
                            action.accept(new R2OMap.BasicEntry<>(R2OEnumMap.this.key[this.curr = this.next], v));
                        }
                        ++this.next;
                    }
                    this.count = max;
                }

                @Override
                public boolean hasNext() {
                    return this.count < R2OEnumMap.this.size;
                }

                @Override
                public Reference2ObjectMap.Entry<K, V> next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    V v = R2OEnumMap.this.value[this.next];
                    while (v == null) {
                        v = R2OEnumMap.this.value[++this.next];
                    }
                    ++this.count;
                    return new R2OMap.BasicEntry<>(R2OEnumMap.this.key[this.curr = this.next++], v);
                }

                @Override
                public void remove() {
                    if (this.curr == -1) {
                        throw new IllegalStateException();
                    }
                    R2OEnumMap.this.value[this.curr] = null;
                    --R2OEnumMap.this.size;
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
            final int oldPos = k.ordinal();
            if (!Objects.equals(v, R2OEnumMap.this.value[oldPos])) {
                return false;
            }
            R2OEnumMap.this.value[oldPos] = null;
            --R2OEnumMap.this.size;
            return true;
        }

        @Override
        public int size() {
            return R2OEnumMap.this.size;
        }

        @Override
        public ObjectSpliterator<Reference2ObjectMap.Entry<K, V>> spliterator() {
            return new EntrySetSpliterator(0, R2OEnumMap.this.size);
        }

        final class EntrySetSpliterator extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Reference2ObjectMap.Entry<K, V>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return ObjectSpliterators.SET_SPLITERATOR_CHARACTERISTICS | Spliterator.SUBSIZED | Spliterator.ORDERED;
            }

            @Override
            protected Reference2ObjectMap.Entry<K, V> get(int location) {
                return new R2OMap.BasicEntry<>(R2OEnumMap.this.key[location], R2OEnumMap.this.value[location]);
            }

            @Override
            protected EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySet.EntrySetSpliterator(pos, maxPos);
            }
        }
    }
}
