package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.ICollectionExtension;

public interface LSet extends LongSet, ICollectionExtension {

    @Nullable Entry fastEntries();

    default boolean removeAll(LSet set) {
        if (set.isEmpty()) {
            return false;
        }
        boolean modified = false;
        for (Entry e = this.fastEntries(); e != null; e = this.fastEntries()) {
            if (set.contains(e.l)) {
                this.remove(e.l);
                modified = true;
            }
        }
        return modified;
    }

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
