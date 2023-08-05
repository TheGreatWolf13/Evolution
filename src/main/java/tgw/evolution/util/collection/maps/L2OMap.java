package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.ICollectionExtension;

public interface L2OMap<V> extends Long2ObjectMap<V>, ICollectionExtension {

    static <V> L2OMap<V> synchronize(L2OMap<V> map) {
        return new Synchronized<>(map);
    }

    @Override
    void clear();

    /**
     * @return An entry to be used in very fast, efficient iteration.<br>
     * {@code null} means the iteration has finished. <br>
     * The Entry itself is mutable and should not be cached anywhere. <br>
     * Entries from the map can be removed during iteration by calling the {@link L2OMap#remove(long)} method as usual, however, the map will not
     * rehash during iteration. If the map was asked to rehash during iteration, it will do so at the end of the process. Any other modification of
     * the map during this process will probably break it, and no checks will be made. You have
     * been warned. <br>
     * The implementation is, of course, NOT thread-safe.
     */
    @Nullable Entry<V> fastEntries();

    class Synchronized<V> extends Long2ObjectMaps.SynchronizedMap<V> implements L2OMap<V> {

        protected final L2OMap<V> map;

        protected Synchronized(L2OMap<V> m) {
            super(m);
            this.map = m;
        }

        @Override
        public @Nullable L2OMap.Entry<V> fastEntries() {
            return this.map.fastEntries();
        }

        @Override
        public void trimCollection() {
            synchronized (this.sync) {
                this.map.trimCollection();
            }
        }
    }

    class Entry<V> {
        protected long k;
        protected @Nullable V v;

        @Contract(pure = true)
        public long key() {
            return this.k;
        }

        @Contract(mutates = "this")
        protected Entry<V> set(long k, @Nullable V v) {
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
}
