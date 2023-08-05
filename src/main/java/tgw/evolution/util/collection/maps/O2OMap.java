package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.ICollectionExtension;

import java.util.Map;
import java.util.Objects;

public interface O2OMap<K, V> extends Object2ObjectMap<K, V>, ICollectionExtension {

    @Override
    void clear();

    /**
     * @return An entry to be used in very fast, efficient iteration.<br>
     * {@code null} means the iteration has finished. <br>
     * The Entry itself is mutable and should not be cached anywhere. <br>
     * Entries from the map can be removed during iteration by calling the {@link O2OMap#remove(Object)} method as usual, however, the map will not
     * rehash during iteration. If the map was asked to rehash during iteration, it will do so at the end of the process. Any other modification of
     * the map during this process will probably break it, and no checks will be made. You have
     * been warned. <br>
     * The implementation is, of course, NOT thread-safe.
     */
    @Nullable Entry<K, V> fastEntries();

    class Entry<K, V> {
        protected @Nullable K k;
        protected @Nullable V v;

        @Contract(pure = true)
        public K key() {
            assert this.k != null;
            return this.k;
        }

        @Contract(mutates = "this")
        protected O2OMap.Entry<K, V> set(@Nullable K k, @Nullable V v) {
            this.k = k;
            this.v = v;
            return this;
        }

        @Contract(pure = true)
        public V value() {
            assert this.v != null;
            return this.v;
        }
    }

    class BasicEntry<K, V> implements Object2ObjectMap.Entry<K, V> {
        protected K key;
        protected V value;

        protected BasicEntry() {
        }

        protected BasicEntry(final K key, final V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            if (o instanceof Object2ObjectMap.Entry) {
                final Object2ObjectMap.Entry<K, V> e = (Object2ObjectMap.Entry<K, V>) o;
                return Objects.equals(this.key, e.getKey()) && Objects.equals(this.value, e.getValue());
            }
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            final Object key = e.getKey();
            final Object value = e.getValue();
            return Objects.equals(this.key, key) && Objects.equals(this.value, value);
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public V getValue() {
            return this.value;
        }

        @Override
        public int hashCode() {
            //noinspection NonFinalFieldReferencedInHashCode
            return (this.key == null ? 0 : this.key.hashCode()) ^ (this.value == null ? 0 : this.value.hashCode());
        }

        @Override
        public V setValue(final V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return this.key + "->" + this.value;
        }
    }
}
