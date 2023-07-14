package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import tgw.evolution.util.collection.ICollectionExtension;

public interface OList<K> extends ObjectList<K>, ICollectionExtension {

    EmptyList EMPTY_LIST = new EmptyList();

    static <K> OList<K> emptyList() {
        return EMPTY_LIST;
    }

    default boolean addAll(Iterable<? extends K> it) {
        boolean added = false;
        for (K k : it) {
            added |= this.add(k);
        }
        return added;
    }

    void addMany(K value, int length);

    /**
     * Start is inclusive, end is exclusive
     */
    void setMany(K value, int start, int end);

    class EmptyList<K> extends ObjectLists.EmptyList<K> implements OList<K> {

        protected EmptyList() {
        }

        @Override
        public boolean addAll(Iterable<? extends K> it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(K value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(K value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trimCollection() {
        }
    }
}
