package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.ICollectionExtension;

public interface ISet extends IntSet, ICollectionExtension {

    default boolean addAll(ISet set) {
        boolean modified = false;
        for (Entry e = set.fastEntries(); e != null; e = set.fastEntries()) {
            if (this.add(e.i)) {
                modified = true;
            }
        }
        return modified;
    }

    default boolean containsAll(ISet set) {
        for (Entry e = set.fastEntries(); e != null; e = set.fastEntries()) {
            if (!this.contains(e.i)) {
                set.resetIteration();
                return false;
            }
        }
        return true;
    }

    @Nullable Entry fastEntries();

    default boolean removeAll(ISet set) {
        boolean modified = false;
        for (Entry e = this.fastEntries(); e != null; e = this.fastEntries()) {
            if (set.contains(e.i)) {
                this.remove(e.i);
                modified = true;
            }
        }
        return modified;
    }

    void resetIteration();

    class Entry {
        protected int i;

        public int get() {
            return this.i;
        }

        protected Entry set(int i) {
            this.i = i;
            return this;
        }
    }
}
