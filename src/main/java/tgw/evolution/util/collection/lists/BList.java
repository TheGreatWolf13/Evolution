package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.bytes.ByteIterable;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.bytes.ByteLists;
import org.jetbrains.annotations.UnmodifiableView;

public interface BList extends ByteList, ListExtension {

    static @UnmodifiableView BList emptyList() {
        return EmptyList.EMPTY_LIST;
    }

    static @UnmodifiableView BList of(byte... ks) {
        return switch (ks.length) {
            case 0 -> emptyList();
            case 1 -> singleton(ks[0]);
            default -> {
                BList list = new BArrayList(ks);
                list.trim();
                yield list.view();
            }
        };
    }

    static @UnmodifiableView BList of() {
        return emptyList();
    }

    static @UnmodifiableView BList of(byte k) {
        return singleton(k);
    }

    static @UnmodifiableView BList singleton(byte k) {
        return new Singleton(k);
    }

    default boolean addAll(ByteIterable it) {
        boolean added = false;
        ByteIterator iterator = it.iterator();
        while (iterator.hasNext()) {
            added |= this.add(iterator.nextByte());
        }
        return added;
    }

    /**
     * Add many ({@code length}) equal elements ({@code value}) to the list efficiently.
     */
    void addMany(byte value, int length);

    /**
     * Start is inclusive, end is exclusive
     */
    void setMany(byte value, int start, int end);

    @UnmodifiableView BList view();

    class EmptyList extends ByteLists.EmptyList implements BList {

        protected static final EmptyList EMPTY_LIST = new EmptyList();

        protected EmptyList() {
        }

        @Override
        public boolean addAll(ByteIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(byte value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(byte value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trim() {
        }

        @Override
        public @UnmodifiableView BList view() {
            return this;
        }
    }

    class Singleton extends ByteLists.Singleton implements BList {

        public Singleton(byte element) {
            super(element);
        }

        @Override
        public boolean addAll(ByteIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(byte value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(byte value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trim() {
        }

        @Override
        public @UnmodifiableView BList view() {
            return this;
        }
    }

    class View extends ByteLists.UnmodifiableList implements BList {

        protected View(BList l) {
            super(l);
        }

        @Override
        public boolean addAll(ByteIterable it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMany(byte value, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMany(byte value, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trim() {
        }

        @Override
        public @UnmodifiableView BList view() {
            return this;
        }
    }
}
