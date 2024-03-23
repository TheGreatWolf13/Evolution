package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface ISet extends IntSet, SetEv {

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

    long beginIteration();

    int getIteration(long it);

    int getSampleElement();

    long nextEntry(long it);

    void removeIteration(long it);

    @UnmodifiableView ISet view();

    class EmptySet extends IntSets.EmptySet implements ISet {

        protected static final EmptySet EMPTY = new EmptySet();

        protected EmptySet() {
        }

        @Override
        public long beginIteration() {
            return 0;
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
        public long nextEntry(long it) {
            return 0;
        }

        @Override
        public void removeIteration(long it) {
            throw new UnsupportedOperationException();
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
        public long beginIteration() {
            return 1;
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
        public void removeIteration(long it) {
            throw new UnsupportedOperationException();
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
        public long beginIteration() {
            return this.set.beginIteration();
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
        public long nextEntry(long it) {
            return this.set.nextEntry(it);
        }

        @Override
        public void removeIteration(long it) {
            throw new UnsupportedOperationException();
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
