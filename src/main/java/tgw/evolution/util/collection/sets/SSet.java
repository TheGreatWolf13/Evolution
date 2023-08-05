package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.ICollectionExtension;

public interface SSet extends ShortSet, ICollectionExtension {

    @Nullable Entry fastEntries();

    /**
     * Gets the "first" element of this set.
     */
    @Nullable Entry getElement();

    class Entry {
        protected short s;

        public short get() {
            return this.s;
        }

        protected Entry set(short s) {
            this.s = s;
            return this;
        }
    }
}
