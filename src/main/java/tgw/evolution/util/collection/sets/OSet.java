package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.ICollectionExtension;
import tgw.evolution.util.collection.lists.OList;

public interface OSet<K> extends ObjectSet<K>, ICollectionExtension {

    default boolean addAll(OList<? extends K> list) {
        boolean modified = false;
        for (int i = 0, len = list.size(); i < len; ++i) {
            if (this.add(list.get(i))) {
                modified = true;
            }
        }
        return modified;
    }

    @Nullable K fastEntries();

    /**
     * Gets the "first" element of this set.
     */
    @Nullable K getElement();

    @UnmodifiableView OSet<K> view();

    class View<K> extends ObjectSets.UnmodifiableSet<K> implements OSet<K> {

        protected final OSet<K> s;

        protected View(OSet<K> s) {
            super(s);
            this.s = s;
        }

        @Override
        public @Nullable K fastEntries() {
            return this.s.fastEntries();
        }

        @Override
        public @Nullable K getElement() {
            return this.s.getElement();
        }

        @Override
        public void trimCollection() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @UnmodifiableView OSet<K> view() {
            return this;
        }
    }
}
