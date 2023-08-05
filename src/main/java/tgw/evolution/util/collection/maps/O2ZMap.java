package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.ICollectionExtension;

public interface O2ZMap<K> extends Object2BooleanMap<K>, ICollectionExtension {

    @Override
    void clear();

    /**
     * @return An entry to be used in very fast, efficient iteration.<br>
     * {@code null} means the iteration has finished. <br>
     * The Entry itself is mutable and should not be cached anywhere. <br>
     * Entries from the map can be removed during iteration by calling the {@link O2ZMap#remove(Object)} method as usual, however, the map will not
     * rehash during iteration. If the map was asked to rehash during iteration, it will do so at the end of the process. Any other modification of
     * the map during this process will probably break it, and no checks will be made. You have
     * been warned. <br>
     * The implementation is, of course, NOT thread-safe.
     */
    @Nullable Entry<K> fastEntries();

    @UnmodifiableView O2ZMap<K> view();

    class Entry<K> {
        protected @Nullable K k;
        protected boolean v;

        @Contract(pure = true)
        public K key() {
            assert this.k != null;
            return this.k;
        }

        @Contract(mutates = "this")
        protected Entry<K> set(@Nullable K k, boolean v) {
            this.k = k;
            this.v = v;
            return this;
        }

        @Contract(pure = true)
        public boolean value() {
            return this.v;
        }
    }

    class View<K> extends Object2BooleanMaps.UnmodifiableMap<K> implements O2ZMap<K> {

        protected final O2ZMap<K> map;

        protected View(O2ZMap<K> m) {
            super(m);
            this.map = m;
        }

        @Override
        public @Nullable O2ZMap.Entry<K> fastEntries() {
            return this.map.fastEntries();
        }

        @Override
        public void trimCollection() {
        }

        @Override
        public @UnmodifiableView O2ZMap<K> view() {
            return this;
        }
    }
}
