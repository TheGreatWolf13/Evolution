package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.floats.FloatIterable;
import it.unimi.dsi.fastutil.floats.FloatIterator;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.floats.FloatLists;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.ICollectionExtension;

public interface FList extends FloatList, ICollectionExtension {

    static @UnmodifiableView FList emptyList() {
        return EmptyList.EMPTY_LIST;
    }

    static @UnmodifiableView FList of() {
        return emptyList();
    }

    static @UnmodifiableView FList of(float k) {
        return singleton(k);
    }

    static @UnmodifiableView FList of(float... ks) {
        return switch (ks.length) {
            case 0 -> emptyList();
            case 1 -> singleton(ks[0]);
            default -> {
                FList list = new FArrayList(ks);
                list.trimCollection();
                yield list.view();
            }
        };
    }

    static @UnmodifiableView FList singleton(float k) {
        return new Singleton(k);
    }

    default boolean addAll(FloatIterable it) {
        boolean added = false;
        FloatIterator iterator = it.iterator();
        while (iterator.hasNext()) {
            added |= this.add(iterator.nextFloat());
        }
        return added;
    }

    /**
     * Add many ({@code length}) equal elements ({@code value}) to the list efficiently.
     */
    void addMany(float value, int length);

    /**
     * Start is inclusive, end is exclusive
     */
    void setMany(float value, int start, int end);

    @UnmodifiableView FList view();

    class Singleton extends FloatLists.Singleton implements FList {

        public Singleton(float element) {
            super(element);
        }

        @Override
        public boolean addAll(FloatIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(float value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(float value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trimCollection() {
        }

        @Override
        public @UnmodifiableView FList view() {
            return this;
        }
    }

    class View extends FloatLists.UnmodifiableList implements FList {

        protected View(FList l) {
            super(l);
        }

        @Override
        public boolean addAll(FloatIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(float value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(float value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trimCollection() {
        }

        @Override
        public @UnmodifiableView FList view() {
            return this;
        }
    }

    class EmptyList extends FloatLists.EmptyList implements FList {

        protected static final EmptyList EMPTY_LIST = new EmptyList();

        protected EmptyList() {
        }

        @Override
        public boolean addAll(FloatIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(float value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(float value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trimCollection() {
        }

        @Override
        public @UnmodifiableView FList view() {
            return this;
        }
    }
}
