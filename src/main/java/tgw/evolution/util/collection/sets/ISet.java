package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.ints.*;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.lists.IList;

import java.util.NoSuchElementException;

public interface ISet extends IntSet, SetExtension {

    static @UnmodifiableView ISet emptySet() {
        return ISet.EmptySet.EMPTY;
    }

    static @UnmodifiableView ISet of() {
        return emptySet();
    }

    static @UnmodifiableView ISet of(int k) {
        return singleton(k);
    }

    static @UnmodifiableView ISet of(int... ks) {
        return switch (ks.length) {
            case 0 -> emptySet();
            case 1 -> singleton(ks[0]);
            default -> {
                ISet set = new IHashSet(ks);
                set.trim();
                yield set.view();
            }
        };
    }

    static @UnmodifiableView ISet singleton(int k) {
        return new ISet.Singleton(k);
    }

    default boolean addAll(ISet set) {
        this.preAllocate(set.size());
        boolean modified = false;
        for (long it = set.beginIteration(); set.hasNextIteration(it); it = set.nextEntry(it)) {
            if (this.add(set.getIteration(it))) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    default boolean addAll(IntCollection c) {
        if (c instanceof ISet set) {
            return this.addAll(set);
        }
        this.preAllocate(c.size());
        boolean retVal = false;
        IntIterator i = c.iterator();
        while (i.hasNext()) {
            if (this.add(i.nextInt())) {
                retVal = true;
            }
        }
        return retVal;
    }

    long beginIteration();

    default boolean containsAll(IList c) {
        for (int i = 0, len = c.size(); i < len; ++i) {
            if (!this.contains(c.getInt(i))) {
                return false;
            }
        }
        return true;
    }

    default boolean containsAll(ISet c) {
        for (long it = c.beginIteration(); c.hasNextIteration(it); it = c.nextEntry(it)) {
            if (!this.contains(c.getIteration(it))) {
                return false;
            }
        }
        return true;
    }

    @Override
    default boolean containsAll(IntCollection c) {
        if (c instanceof ISet set) {
            return this.containsAll(set);
        }
        if (c instanceof IList list) {
            return this.containsAll(list);
        }
        IntIterator i = c.iterator();
        do {
            if (!i.hasNext()) {
                return true;
            }
        } while (this.contains(i.nextInt()));
        return false;
    }

    int getIteration(long it);

    int getSampleElement();

    long nextEntry(long it);

    default void preAllocate(int extraSize) {

    }

    long removeIteration(long it);

    @Override
    default int[] toIntArray() {
        int[] array = new int[this.size()];
        int i = 0;
        for (long it = this.beginIteration(); this.hasNextIteration(it); it = this.nextEntry(it)) {
            array[i++] = this.getIteration(it);
        }
        return array;
    }

    @UnmodifiableView ISet view();

    class EmptySet extends IntSets.EmptySet implements ISet {

        protected static final EmptySet EMPTY = new EmptySet();

        protected EmptySet() {
        }

        @Override
        public boolean addAll(IntCollection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public boolean containsAll(IntCollection c) {
            return c.isEmpty();
        }

        @Override
        public int getIteration(long it) {
            throw new NoSuchElementException("Empty set");
        }

        @Override
        public int getSampleElement() {
            throw new NoSuchElementException("Empty set");
        }

        @Override
        public boolean hasNextIteration(long it) {
            return false;
        }

        @Override
        public long nextEntry(long it) {
            return 0;
        }

        @Override
        public long removeIteration(long it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int[] toIntArray() {
            return IntArrays.EMPTY_ARRAY;
        }

        @Override
        public boolean trim() {
            return false;
        }

        @Override
        public @UnmodifiableView ISet view() {
            return this;
        }
    }

    class Singleton extends IntSets.Singleton implements ISet {

        protected Singleton(int element) {
            super(element);
        }

        @Override
        public boolean addAll(IntCollection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long beginIteration() {
            return 1;
        }

        @Override
        public boolean containsAll(IntCollection c) {
            return c.size() <= 1 && ISet.super.containsAll(c);
        }

        @Override
        public int getIteration(long it) {
            if (it != 1) {
                throw new NoSuchElementException();
            }
            return this.element;
        }

        @Override
        public int getSampleElement() {
            return this.element;
        }

        @Override
        public long nextEntry(long it) {
            return 0;
        }

        @Override
        public long removeIteration(long it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int[] toIntArray() {
            return new int[]{this.element};
        }

        @Override
        public boolean trim() {
            return false;
        }

        @Override
        public @UnmodifiableView ISet view() {
            return this;
        }
    }

    class View extends IntSets.UnmodifiableSet implements ISet {

        protected final ISet set;

        protected View(ISet s) {
            super(s);
            this.set = s;
        }

        @Override
        public boolean addAll(IntCollection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long beginIteration() {
            return this.set.beginIteration();
        }

        @Override
        public boolean containsAll(IntCollection c) {
            return this.set.containsAll(c);
        }

        @Override
        public int getIteration(long it) {
            return this.set.getIteration(it);
        }

        @Override
        public int getSampleElement() {
            return this.set.getSampleElement();
        }

        @Override
        public boolean hasNextIteration(long it) {
            return this.set.hasNextIteration(it);
        }

        @Override
        public long nextEntry(long it) {
            return this.set.nextEntry(it);
        }

        @Override
        public long removeIteration(long it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int[] toIntArray() {
            return this.set.toIntArray();
        }

        @Override
        public boolean trim() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @UnmodifiableView ISet view() {
            return this;
        }
    }
}
