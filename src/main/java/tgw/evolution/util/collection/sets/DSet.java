package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.doubles.DoubleSets;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface DSet extends DoubleSet, SetExtension {

    static @UnmodifiableView DSet emptySet() {
        return EmptySet.EMPTY;
    }

    static @UnmodifiableView DSet of() {
        return emptySet();
    }

    static @UnmodifiableView DSet of(double k) {
        return singleton(k);
    }

    static @UnmodifiableView DSet of(double... ks) {
        return switch (ks.length) {
            case 0 -> emptySet();
            case 1 -> singleton(ks[0]);
            default -> {
                DSet set = new DHashSet(ks);
                set.trim();
                yield set.view();
            }
        };
    }

    static @UnmodifiableView DSet singleton(double k) {
        return new Singleton(k);
    }

    long beginIteration();

    double getIteration(long it);

    double getSampleElement();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView DSet view();

    class EmptySet extends DoubleSets.EmptySet implements DSet {

        protected static final EmptySet EMPTY = new EmptySet();

        protected EmptySet() {
        }

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public double getIteration(long it) {
            throw new NoSuchElementException("Empty set");
        }

        @Override
        public double getSampleElement() {
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
        public boolean trim() {
            return false;
        }

        @Override
        public @UnmodifiableView DSet view() {
            return this;
        }
    }

    class Singleton extends DoubleSets.Singleton implements DSet {

        protected Singleton(double element) {
            super(element);
        }

        @Override
        public long beginIteration() {
            return 1;
        }

        @Override
        public double getIteration(long it) {
            if (it != 1) {
                throw new NoSuchElementException();
            }
            return this.element;
        }

        @Override
        public double getSampleElement() {
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
        public boolean trim() {
            return false;
        }

        @Override
        public @UnmodifiableView DSet view() {
            return this;
        }
    }

    class View extends DoubleSets.UnmodifiableSet implements DSet {

        protected final DSet set;

        protected View(DSet s) {
            super(s);
            this.set = s;
        }

        @Override
        public long beginIteration() {
            return this.set.beginIteration();
        }

        @Override
        public double getIteration(long it) {
            return this.set.getIteration(it);
        }

        @Override
        public double getSampleElement() {
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
        public boolean trim() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @UnmodifiableView DSet view() {
            return this;
        }
    }
}
