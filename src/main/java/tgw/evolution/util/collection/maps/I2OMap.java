package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.ICollectionExtension;

public interface I2OMap<V> extends Int2ObjectMap<V>, ICollectionExtension {

    @Override
    void clear();

    /**
     * @return An entry to be used in very fast, efficient iteration.<br>
     * {@code null} means the iteration has finished. <br>
     * The Entry itself is mutable and should not be cached anywhere. <br>
     * Entries from the map can be removed during iteration by calling the {@link I2OMap#remove(int)} method as usual, however, the map will not
     * rehash during iteration. If the map was asked to rehash during iteration, it will do so at the end of the process. Any other modification of
     * the map during this process will probably break it, and no checks will be made. You have
     * been warned. <br>
     * The implementation is, of course, NOT thread-safe.
     */
    @Nullable I2OMap.Entry<V> fastEntries();

    class Entry<V> {
        protected int k;
        protected @Nullable V v;

        @Contract(pure = true)
        public int key() {
            return this.k;
        }

        @Contract(mutates = "this")
        protected Entry<V> set(int k, @Nullable V v) {
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
