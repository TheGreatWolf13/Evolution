package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.ICollectionExtension;

public interface R2OMap<K, V> extends Reference2ObjectMap<K, V>, ICollectionExtension {

    R2OMap.EmptyMap EMPTY_MAP = new R2OMap.EmptyMap();

    static <K, V> R2OMap<K, V> emptyMap() {
        return EMPTY_MAP;
    }

    @Override
    void clear();

    /**
     * @return An entry to be used in very fast, efficient iteration.<br>
     * {@code null} means the iteration has finished. <br>
     * The Entry itself is mutable and should not be cached anywhere. <br>
     * Entries from the map can be removed during iteration by calling the {@link R2OMap#remove(Object)} method as usual, however, the map will not
     * rehash during iteration. If the map was asked to rehash during iteration, it will do so at the end of the process. Any other modification of
     * the map during this process will probably break it, and no checks will be made. You have
     * been warned. <br>
     * The implementation is, of course, NOT thread-safe.
     */
    @Nullable Entry<K, V> fastEntries();

    @UnmodifiableView R2OMap<K, V> view();

    class EmptyMap<K, V> extends Reference2ObjectMaps.EmptyMap<K, V> implements R2OMap<K, V> {

        protected EmptyMap() {
        }

        @Override
        public @Nullable R2OMap.Entry<K, V> fastEntries() {
            return null;
        }

        @Override
        public void trimCollection() {
        }

        @Override
        public @UnmodifiableView R2OMap<K, V> view() {
            return this;
        }
    }

    class Entry<K, V> {
        protected @Nullable K k;
        protected @Nullable V v;

        @Contract(pure = true)
        public K key() {
            assert this.k != null;
            return this.k;
        }

        @Contract(mutates = "this")
        protected Entry<K, V> set(@Nullable K k, @Nullable V v) {
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

    class View<K, V> extends Reference2ObjectMaps.UnmodifiableMap<K, V> implements R2OMap<K, V> {

        protected final R2OMap<K, V> map;

        protected View(R2OMap<K, V> m) {
            super(m);
            this.map = m;
        }

        @Override
        public @Nullable R2OMap.Entry<K, V> fastEntries() {
            return this.map.fastEntries();
        }

        @Override
        public void trimCollection() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @UnmodifiableView R2OMap<K, V> view() {
            return this;
        }
    }
}
