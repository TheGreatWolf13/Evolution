package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.bytes.ByteSet;
import it.unimi.dsi.fastutil.bytes.ByteSets;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface BSet extends ByteSet, SetEv {

    static @UnmodifiableView BSet emptySet() {
        return EmptySet.EMPTY;
    }

    static @UnmodifiableView BSet of() {
        return emptySet();
    }

    static @UnmodifiableView BSet of(byte k) {
        return singleton(k);
    }

    static @UnmodifiableView BSet of(byte... ks) {
        return switch (ks.length) {
            case 0 -> emptySet();
            case 1 -> singleton(ks[0]);
            default -> {
                BSet set = new BHashSet(ks);
                set.trim();
                yield set.view();
            }
        };
    }

    static @UnmodifiableView BSet singleton(byte k) {
        return new Singleton(k);
    }

    long beginIteration();

    byte getIteration(long it);

    byte getSampleElement();

    long nextEntry(long it);

    void removeIteration(long it);

    @UnmodifiableView BSet view();

    class EmptySet extends ByteSets.EmptySet implements BSet {

        protected static final EmptySet EMPTY = new EmptySet();

        protected EmptySet() {
        }

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public byte getIteration(long it) {
            throw new NoSuchElementException("Empty set");
        }

        @Override
        public byte getSampleElement() {
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
        public @UnmodifiableView BSet view() {
            return this;
        }
    }

    class Singleton extends ByteSets.Singleton implements BSet {

        protected Singleton(byte element) {
            super(element);
        }

        @Override
        public long beginIteration() {
            return 1;
        }

        @Override
        public byte getIteration(long it) {
            if (it != 1) {
                throw new NoSuchElementException();
            }
            return this.element;
        }

        @Override
        public byte getSampleElement() {
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
        public @UnmodifiableView BSet view() {
            return this;
        }
    }

    class View extends ByteSets.UnmodifiableSet implements BSet {

        protected final BSet set;

        protected View(BSet s) {
            super(s);
            this.set = s;
        }

        @Override
        public long beginIteration() {
            return this.set.beginIteration();
        }

        @Override
        public byte getIteration(long it) {
            return this.set.getIteration(it);
        }

        @Override
        public byte getSampleElement() {
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
        public @UnmodifiableView BSet view() {
            return this;
        }
    }
}
