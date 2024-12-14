package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import it.unimi.dsi.fastutil.shorts.ShortSets;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface SSet extends ShortSet, SetExtension {

    static @UnmodifiableView SSet emptySet() {
        return EmptySet.EMPTY;
    }

    static @UnmodifiableView SSet of() {
        return emptySet();
    }

    static @UnmodifiableView SSet of(short k) {
        return singleton(k);
    }

    static @UnmodifiableView SSet of(short... ks) {
        return switch (ks.length) {
            case 0 -> emptySet();
            case 1 -> singleton(ks[0]);
            default -> {
                SSet set = new SHashSet(ks);
                set.trim();
                yield set.view();
            }
        };
    }

    static @UnmodifiableView SSet singleton(short k) {
        return new Singleton(k);
    }

    long beginIteration();

    short getIteration(long it);

    short getSampleElement();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView SSet view();

    class EmptySet extends ShortSets.EmptySet implements SSet {

        protected static final EmptySet EMPTY = new EmptySet();

        protected EmptySet() {
        }

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public short getIteration(long it) {
            throw new NoSuchElementException("Empty set");
        }

        @Override
        public short getSampleElement() {
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
        public @UnmodifiableView SSet view() {
            return this;
        }
    }

    class Singleton extends ShortSets.Singleton implements SSet {

        protected Singleton(short element) {
            super(element);
        }

        @Override
        public long beginIteration() {
            return 1;
        }

        @Override
        public short getIteration(long it) {
            if (it != 1) {
                throw new NoSuchElementException();
            }
            return this.element;
        }

        @Override
        public short getSampleElement() {
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
        public @UnmodifiableView SSet view() {
            return this;
        }
    }

    class View extends ShortSets.UnmodifiableSet implements SSet {

        protected final SSet set;

        protected View(SSet s) {
            super(s);
            this.set = s;
        }

        @Override
        public long beginIteration() {
            return this.set.beginIteration();
        }

        @Override
        public short getIteration(long it) {
            return this.set.getIteration(it);
        }

        @Override
        public short getSampleElement() {
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
        public @UnmodifiableView SSet view() {
            return this;
        }
    }
}
