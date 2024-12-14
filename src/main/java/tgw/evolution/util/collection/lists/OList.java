package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.objects.ObjectIterable;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.jetbrains.annotations.UnmodifiableView;

public interface OList<K> extends ObjectList<K>, ListExtension {

    static <K> @UnmodifiableView OList<K> emptyList() {
        return EmptyList.EMPTY_LIST;
    }

    static @UnmodifiableView <K> OList<K> of() {
        return emptyList();
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
                yield list.immutable();
            }
        };
    }

    static @UnmodifiableView <K> OList<K> singleton(K k) {
        return new Singleton<>(k);
    }

    default boolean addAll(ObjectIterable<? extends K> it) {
        boolean added = false;
        for (K k : it) {
            added |= this.add(k);
        }
        return added;
    }

    default boolean addAll(Iterable<? extends K> it) {
        boolean added = false;
        for (K k : it) {
            added |= this.add(k);
        }
        return added;
    }

    /**
     * Add many ({@code length}) equal elements ({@code value}) to the list efficiently.
     */
    void addMany(K value, int length);

    default @UnmodifiableView OList<K> immutable() {
        this.trim();
        return this.view();
    }

    /**
     * Start is inclusive, end is exclusive
     */
    void setMany(K value, int start, int end);

    @UnmodifiableView OList<K> view();

    class EmptyList<K> extends ObjectLists.EmptyList<K> implements OList<K> {

        protected static final EmptyList EMPTY_LIST = new EmptyList();

        protected EmptyList() {
        }

        @Override
        public boolean addAll(Iterable<? extends K> it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(ObjectIterable<? extends K> it) {
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
        public void trim() {
        }

        @Override
        public @UnmodifiableView OList<K> view() {
            return this;
        }
    }

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
        public void trim() {
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
        public void trim() {
        }

        @Override
        public @UnmodifiableView OList<K> view() {
            return this;
        }
    }
}
