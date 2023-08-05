package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.ICollectionExtension;

public interface LSet extends LongSet, ICollectionExtension {

    @Nullable Entry fastEntries();

    class Entry {
        protected long l;

        public long get() {
            return this.l;
        }

        protected Entry set(long l) {
            this.l = l;
            return this;
        }
    }
}
