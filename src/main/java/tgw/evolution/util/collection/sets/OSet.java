package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.lists.OList;

import java.util.Collection;
import java.util.NoSuchElementException;

public interface OSet<K> extends ObjectSet<K>, SetExtension {

    static <K> @UnmodifiableView OSet<K> emptySet() {
        return EmptySet.EMPTY;
    }

    static <K> @UnmodifiableView OSet<K> of() {
        return emptySet();
    }

    static <K> @UnmodifiableView OSet<K> of(K k) {
        return singleton(k);
    }

    @SafeVarargs
    static <K> @UnmodifiableView OSet<K> of(K... ks) {
        return switch (ks.length) {
            case 0 -> emptySet();
            case 1 -> singleton(ks[0]);
            default -> {
                OSet<K> set = new OHashSet<>(ks);
                set.trim();
                yield set.view();
            }
        };
    }

    static <K> @UnmodifiableView OSet<K> singleton(K k) {
        return new Singleton<>(k);
    }

    default boolean addAll(OList<? extends K> list) {
        this.preAllocate(list.size());
        boolean modified = false;
        for (int i = 0, len = list.size(); i < len; ++i) {
            if (this.add(list.get(i))) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    default boolean addAll(Collection<? extends K> c) {
        if (c instanceof OList<? extends K> list) {
            return this.addAll(list);
        }
        this.preAllocate(c.size());
        boolean modified = false;
        for (K e : c) {
            if (this.add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    long beginIteration();

    K getIteration(long it);

    K getSampleElement();

    long nextEntry(long it);

    default void preAllocate(int extraSize) {

    }

    long removeIteration(long it);

    @UnmodifiableView OSet<K> view();

    class EmptySet<K> extends ObjectSets.EmptySet<K> implements OSet<K> {

        protected static final EmptySet EMPTY = new EmptySet();

        protected EmptySet() {
        }

        @Override
        public boolean addAll(Collection<? extends K> c) {
            throw new UnsupportedOperationException();
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
        public @UnmodifiableView OSet<K> view() {
            return this;
        }
    }

    class Singleton<K> extends ObjectSets.Singleton<K> implements OSet<K> {

        protected Singleton(K element) {
            super(element);
        }

        @Override
        public boolean addAll(Collection<? extends K> c) {
            throw new UnsupportedOperationException();
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
        public long removeIteration(long it) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean trim() {
            return false;
        }

        @Override
        public @UnmodifiableView OSet<K> view() {
            return this;
        }
    }

    class View<K> extends ObjectSets.UnmodifiableSet<K> implements OSet<K> {

        protected final OSet<K> set;

        protected View(OSet<K> s) {
            super(s);
            this.set = s;
        }

        @Override
        public boolean addAll(Collection<? extends K> c) {
            throw new UnsupportedOperationException();
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
        public @UnmodifiableView OSet<K> view() {
            return this;
        }
    }
}
