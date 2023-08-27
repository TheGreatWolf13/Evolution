package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.ICollectionExtension;

public interface OList<K> extends ObjectList<K>, ICollectionExtension {

    static <K> @UnmodifiableView OList<K> emptyList() {
        return EmptyList.EMPTY_LIST.view();
    }

    static @UnmodifiableView <K> OList<K> of(K k) {
        return singleton(k);
    }

    @SafeVarargs
    static @UnmodifiableView <K> OList<K> of(K... ks) {
        return switch (ks.length) {
            case 0 -> emptyList();
            case 1 -> singleton(ks[0]);
            default -> {
                OList<K> list = new OArrayList<>(ks);
                list.trimCollection();
                yield list.view();
            }
        };
    }

    static @UnmodifiableView <K> OList<K> singleton(K k) {
        return new Singleton<>(k).view();
    }

    @Contract(mutates = "this")
    default boolean addAll(Iterable<? extends K> it) {
        boolean added = false;
        for (K k : it) {
            added |= this.add(k);
        }
        return added;
    }

    @Contract(mutates = "this")
    void addMany(K value, int length);

    /**
     * Start is inclusive, end is exclusive
     */
    @Contract(mutates = "this")
    void setMany(K value, int start, int end);

    @UnmodifiableView OList<K> view();

    class Singleton<K> extends ObjectLists.Singleton<K> implements OList<K> {

        public Singleton(K element) {
            super(element);
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

        @Override
        public @UnmodifiableView OList<K> view() {
            return this;
        }
    }

    class View<K> extends ObjectLists.UnmodifiableList<K> implements OList<K> {

        protected View(OList<K> l) {
            super(l);
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

        @Override
        public @UnmodifiableView OList<K> view() {
            return this;
        }
    }

    class EmptyList<K> extends ObjectLists.EmptyList<K> implements OList<K> {

        protected static final EmptyList EMPTY_LIST = new EmptyList();

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

        @Override
        public @UnmodifiableView OList<K> view() {
            return this;
        }
    }
}
