package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface LSet extends LongSet, SetExtension {

    static @UnmodifiableView LSet emptySet() {
        return EmptySet.EMPTY;
    }

    static @UnmodifiableView LSet of() {
        return emptySet();
    }

    static @UnmodifiableView LSet of(long k) {
        return singleton(k);
    }

    static @UnmodifiableView LSet of(long... ks) {
        return switch (ks.length) {
            case 0 -> emptySet();
            case 1 -> singleton(ks[0]);
            default -> {
                LSet set = new LHashSet(ks);
                set.trim();
                yield set.view();
            }
        };
    }

    static @UnmodifiableView LSet singleton(long k) {
        return new Singleton(k);
    }

    long beginIteration();

    long getIteration(long it);

    long getSampleElement();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView LSet view();

    class EmptySet extends LongSets.EmptySet implements LSet {

        protected static final EmptySet EMPTY = new EmptySet();

        protected EmptySet() {
        }

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public long getIteration(long it) {
            throw new NoSuchElementException("Empty set");
        }

        @Override
        public long getSampleElement() {
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
        public @UnmodifiableView LSet view() {
            return this;
        }
    }

    class Singleton extends LongSets.Singleton implements LSet {

        protected Singleton(long element) {
            super(element);
        }

        @Override
        public long beginIteration() {
            return 1;
        }

        @Override
        public long getIteration(long it) {
            if (it != 1) {
                throw new NoSuchElementException();
            }
            return this.element;
        }

        @Override
        public long getSampleElement() {
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
        public @UnmodifiableView LSet view() {
            return this;
        }
    }

    class View extends LongSets.UnmodifiableSet implements LSet {

        protected final LSet set;

        protected View(LSet s) {
            super(s);
            this.set = s;
        }

        @Override
        public long beginIteration() {
            return this.set.beginIteration();
        }

        @Override
        public long getIteration(long it) {
            return this.set.getIteration(it);
        }

        @Override
        public long getSampleElement() {
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
        public @UnmodifiableView LSet view() {
            return this;
        }
    }
}
