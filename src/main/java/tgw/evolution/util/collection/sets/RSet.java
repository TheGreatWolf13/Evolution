package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface RSet<K> extends ReferenceSet<K>, SetEv {

    static <K> @UnmodifiableView RSet<K> emptySet() {
        return EmptySet.EMPTY;
    }

    static <K> @UnmodifiableView RSet<K> of() {
        return emptySet();
    }

    static <K> @UnmodifiableView RSet<K> of(K k) {
        return singleton(k);
    }

    @SafeVarargs
    static <K> @UnmodifiableView RSet<K> of(K... ks) {
        return switch (ks.length) {
            case 0 -> emptySet();
            case 1 -> singleton(ks[0]);
            default -> {
                RSet<K> set = new RHashSet<>(ks);
                set.trim();
                yield set.view();
            }
        };
    }

    static <K> @UnmodifiableView RSet<K> singleton(K k) {
        return new Singleton<>(k);
    }

    long beginIteration();

    K getIteration(long it);

    K getSampleElement();

    long nextEntry(long it);

    void removeIteration(long it);

    @UnmodifiableView RSet<K> view();

    class EmptySet<K> extends ReferenceSets.EmptySet<K> implements RSet<K> {

        protected static final EmptySet EMPTY = new EmptySet();

        protected EmptySet() {
        }

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public K getIteration(long it) {
            throw new NoSuchElementException("Empty set");
        }

        @Override
        public K getSampleElement() {
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
        public @UnmodifiableView RSet<K> view() {
            return this;
        }
    }

    class Singleton<K> extends ReferenceSets.Singleton<K> implements RSet<K> {

        protected Singleton(K element) {
            super(element);
        }

        @Override
        public long beginIteration() {
            return 1;
        }

        @Override
        public K getIteration(long it) {
            if (it != 1) {
                throw new NoSuchElementException();
            }
            return this.element;
        }

        @Override
        public K getSampleElement() {
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
        public @UnmodifiableView RSet<K> view() {
            return this;
        }
    }

    class View<K> extends ReferenceSets.UnmodifiableSet<K> implements RSet<K> {

        protected final RSet<K> set;

        protected View(RSet<K> s) {
            super(s);
            this.set = s;
        }

        @Override
        public long beginIteration() {
            return this.set.beginIteration();
        }

        @Override
        public K getIteration(long it) {
            return this.set.getIteration(it);
        }

        @Override
        public K getSampleElement() {
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
        public @UnmodifiableView RSet<K> view() {
            return this;
        }
    }
}
