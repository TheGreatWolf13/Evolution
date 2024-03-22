package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.doubles.DoubleIterable;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.ICollectionExtension;

public interface DList extends DoubleList, ICollectionExtension {

    static @UnmodifiableView DList emptyList() {
        return EmptyList.EMPTY_LIST;
    }

    static @UnmodifiableView DList of() {
        return emptyList();
    }

    static @UnmodifiableView DList of(double k) {
        return singleton(k);
    }

    static @UnmodifiableView DList of(double... ks) {
        return switch (ks.length) {
            case 0 -> emptyList();
            case 1 -> singleton(ks[0]);
            default -> {
                DList list = new DArrayList(ks);
                list.trimCollection();
                yield list.view();
            }
        };
    }

    static @UnmodifiableView DList singleton(double k) {
        return new Singleton(k);
    }

    default boolean addAll(DoubleIterable it) {
        boolean added = false;
        DoubleIterator iterator = it.iterator();
        while (iterator.hasNext()) {
            added |= this.add(iterator.nextDouble());
        }
        return added;
    }

    /**
     * Add many ({@code length}) equal elements ({@code value}) to the list efficiently.
     */
    void addMany(double value, int length);

    /**
     * Start is inclusive, end is exclusive
     */
    void setMany(double value, int start, int end);

    @UnmodifiableView DList view();

    class Singleton extends DoubleLists.Singleton implements DList {

        public Singleton(double element) {
            super(element);
        }

        @Override
        public boolean addAll(DoubleIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(double value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(double value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trimCollection() {
        }

        @Override
        public @UnmodifiableView DList view() {
            return this;
        }
    }

    class View extends DoubleLists.UnmodifiableList implements DList {

        protected View(DList l) {
            super(l);
        }

        @Override
        public boolean addAll(DoubleIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(double value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(double value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trimCollection() {
        }

        @Override
        public @UnmodifiableView DList view() {
            return this;
        }
    }

    class EmptyList extends DoubleLists.EmptyList implements DList {

        protected static final EmptyList EMPTY_LIST = new EmptyList();

        protected EmptyList() {
        }

        @Override
        public boolean addAll(DoubleIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(double value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(double value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trimCollection() {
        }

        @Override
        public @UnmodifiableView DList view() {
            return this;
        }
    }
}
