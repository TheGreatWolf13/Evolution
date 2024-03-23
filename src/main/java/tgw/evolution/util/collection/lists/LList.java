package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import org.jetbrains.annotations.UnmodifiableView;

public interface LList extends LongList, ListEv {

    static @UnmodifiableView LList emptyList() {
        return EmptyList.EMPTY_LIST;
    }

    static @UnmodifiableView LList of(long... ks) {
        return switch (ks.length) {
            case 0 -> emptyList();
            case 1 -> singleton(ks[0]);
            default -> {
                LList list = new LArrayList(ks);
                list.trim();
                yield list.view();
            }
        };
    }

    static @UnmodifiableView LList of() {
        return emptyList();
    }

    static @UnmodifiableView LList of(long k) {
        return singleton(k);
    }

    static @UnmodifiableView LList singleton(long k) {
        return new Singleton(k);
    }

    default boolean addAll(LongIterable it) {
        boolean added = false;
        LongIterator iterator = it.iterator();
        while (iterator.hasNext()) {
            added |= this.add(iterator.nextLong());
        }
        return added;
    }

    /**
     * Add many ({@code length}) equal elements ({@code value}) to the list efficiently.
     */
    void addMany(long value, int length);

    /**
     * Start is inclusive, end is exclusive
     */
    void setMany(long value, int start, int end);

    @UnmodifiableView LList view();

    class EmptyList extends LongLists.EmptyList implements LList {

        protected static final EmptyList EMPTY_LIST = new EmptyList();

        protected EmptyList() {
        }

        @Override
        public boolean addAll(LongIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(long value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(long value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trim() {
        }

        @Override
        public @UnmodifiableView LList view() {
            return this;
        }
    }

    class Singleton extends LongLists.Singleton implements LList {

        public Singleton(long element) {
            super(element);
        }

        @Override
        public boolean addAll(LongIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(long value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(long value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trim() {
        }

        @Override
        public @UnmodifiableView LList view() {
            return this;
        }
    }

    class View extends LongLists.UnmodifiableList implements LList {

        protected View(LList l) {
            super(l);
        }

        @Override
        public boolean addAll(LongIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(long value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(long value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trim() {
        }

        @Override
        public @UnmodifiableView LList view() {
            return this;
        }
    }
}
