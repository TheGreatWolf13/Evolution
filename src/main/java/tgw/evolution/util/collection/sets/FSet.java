package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.floats.FloatSet;
import it.unimi.dsi.fastutil.floats.FloatSets;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface FSet extends FloatSet, SetEv {

    static @UnmodifiableView FSet emptySet() {
        return EmptySet.EMPTY;
    }

    static @UnmodifiableView FSet of() {
        return emptySet();
    }

    static @UnmodifiableView FSet of(float k) {
        return singleton(k);
    }

    static @UnmodifiableView FSet of(float... ks) {
        return switch (ks.length) {
            case 0 -> emptySet();
            case 1 -> singleton(ks[0]);
            default -> {
                FSet set = new FHashSet(ks);
                set.trim();
                yield set.view();
            }
        };
    }

    static @UnmodifiableView FSet singleton(float k) {
        return new Singleton(k);
    }

    long beginIteration();

    float getIteration(long it);

    float getSampleElement();

    long nextEntry(long it);

    void removeIteration(long it);

    @UnmodifiableView FSet view();

    class EmptySet extends FloatSets.EmptySet implements FSet {

        protected static final EmptySet EMPTY = new EmptySet();

        protected EmptySet() {
        }

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public float getIteration(long it) {
            throw new NoSuchElementException("Empty set");
        }

        @Override
        public float getSampleElement() {
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
        public @UnmodifiableView FSet view() {
            return this;
        }
    }

    class Singleton extends FloatSets.Singleton implements FSet {

        protected Singleton(float element) {
            super(element);
        }

        @Override
        public long beginIteration() {
            return 1;
        }

        @Override
        public float getIteration(long it) {
            if (it != 1) {
                throw new NoSuchElementException();
            }
            return this.element;
        }

        @Override
        public float getSampleElement() {
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
        public @UnmodifiableView FSet view() {
            return this;
        }
    }

    class View extends FloatSets.UnmodifiableSet implements FSet {

        protected final FSet set;

        protected View(FSet s) {
            super(s);
            this.set = s;
        }

        @Override
        public long beginIteration() {
            return this.set.beginIteration();
        }

        @Override
        public float getIteration(long it) {
            return this.set.getIteration(it);
        }

        @Override
        public float getSampleElement() {
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
        public @UnmodifiableView FSet view() {
            return this;
        }
    }
}
