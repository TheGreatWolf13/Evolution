package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.shorts.ShortIterable;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortLists;
import org.jetbrains.annotations.UnmodifiableView;

public interface SList extends ShortList, ListExtension {

    static @UnmodifiableView SList emptyList() {
        return EmptyList.EMPTY_LIST;
    }

    static @UnmodifiableView SList of(short... ks) {
        return switch (ks.length) {
            case 0 -> emptyList();
            case 1 -> singleton(ks[0]);
            default -> {
                SList list = new SArrayList(ks);
                list.trim();
                yield list.view();
            }
        };
    }

    static @UnmodifiableView SList of() {
        return emptyList();
    }

    static @UnmodifiableView SList of(short k) {
        return singleton(k);
    }

    static @UnmodifiableView SList singleton(short k) {
        return new Singleton(k);
    }

    default boolean addAll(ShortIterable it) {
        boolean added = false;
        ShortIterator iterator = it.iterator();
        while (iterator.hasNext()) {
            added |= this.add(iterator.nextShort());
        }
        return added;
    }

    /**
     * Add many ({@code length}) equal elements ({@code value}) to the list efficiently.
     */
    void addMany(short value, int length);

    /**
     * Start is inclusive, end is exclusive
     */
    void setMany(short value, int start, int end);

    @UnmodifiableView SList view();

    class EmptyList extends ShortLists.EmptyList implements SList {

        protected static final EmptyList EMPTY_LIST = new EmptyList();

        protected EmptyList() {
        }

        @Override
        public boolean addAll(ShortIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(short value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(short value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trim() {
        }

        @Override
        public @UnmodifiableView SList view() {
            return this;
        }
    }

    class Singleton extends ShortLists.Singleton implements SList {

        public Singleton(short element) {
            super(element);
        }

        @Override
        public boolean addAll(ShortIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(short value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(short value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trim() {
        }

        @Override
        public @UnmodifiableView SList view() {
            return this;
        }
    }

    class View extends ShortLists.UnmodifiableList implements SList {

        protected View(SList l) {
            super(l);
        }

        @Override
        public boolean addAll(ShortIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(short value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(short value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trim() {
        }

        @Override
        public @UnmodifiableView SList view() {
            return this;
        }
    }
}
