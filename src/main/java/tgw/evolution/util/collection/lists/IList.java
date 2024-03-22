package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.ICollectionExtension;

public interface IList extends IntList, ICollectionExtension {

    static @UnmodifiableView IList emptyList() {
        return EmptyList.EMPTY_LIST;
    }

    static @UnmodifiableView IList of() {
        return emptyList();
    }

    static @UnmodifiableView IList of(int k) {
        return singleton(k);
    }

    static @UnmodifiableView IList of(int... ks) {
        return switch (ks.length) {
            case 0 -> emptyList();
            case 1 -> singleton(ks[0]);
            default -> {
                IList list = new IArrayList(ks);
                list.trimCollection();
                yield list.view();
            }
        };
    }

    static @UnmodifiableView IList singleton(int k) {
        return new Singleton(k);
    }

    default boolean addAll(IntIterable it) {
        boolean added = false;
        IntIterator iterator = it.iterator();
        while (iterator.hasNext()) {
            added |= this.add(iterator.nextInt());
        }
        return added;
    }

    /**
     * Add many ({@code length}) equal elements ({@code value}) to the list efficiently.
     */
    void addMany(int value, int length);

    /**
     * Start is inclusive, end is exclusive
     */
    void setMany(int value, int start, int end);

    @UnmodifiableView IList view();

    class Singleton extends IntLists.Singleton implements IList {

        public Singleton(int element) {
            super(element);
        }

        @Override
        public boolean addAll(IntIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(int value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(int value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trimCollection() {
        }

        @Override
        public @UnmodifiableView IList view() {
            return this;
        }
    }

    class View extends IntLists.UnmodifiableList implements IList {

        protected View(IList l) {
            super(l);
        }

        @Override
        public boolean addAll(IntIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(int value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(int value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trimCollection() {
        }

        @Override
        public @UnmodifiableView IList view() {
            return this;
        }
    }

    class EmptyList extends IntLists.EmptyList implements IList {

        protected static final EmptyList EMPTY_LIST = new EmptyList();

        protected EmptyList() {
        }

        @Override
        public boolean addAll(IntIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(int value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(int value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trimCollection() {
        }

        @Override
        public @UnmodifiableView IList view() {
            return this;
        }
    }
}
