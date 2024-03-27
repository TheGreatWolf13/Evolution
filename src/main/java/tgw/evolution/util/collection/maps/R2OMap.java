package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface R2OMap<K, V> extends Reference2ObjectMap<K, V>, MapEv {

    static @UnmodifiableView <K, V> R2OMap<K, V> emptyMap() {
        return EmptyMap.EMPTY;
    }

    static @UnmodifiableView <K, V> R2OMap<K, V> of() {
        return emptyMap();
    }

    long beginIteration();

    @Override
    void clear();

    @Nullable K getIterationKey(long it);

    V getIterationValue(long it);

    @Nullable K getSampleKey();

    V getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView R2OMap<K, V> view();

    class EmptyMap<K, V> extends Reference2ObjectMaps.EmptyMap<K, V> implements R2OMap<K, V> {

        protected static final EmptyMap EMPTY = new EmptyMap();

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public K getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public V getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public K getSampleKey() {
            throw new NoSuchElementException();
        }

        @Override
        public V getSampleValue() {
            throw new NoSuchElementException();
        }

        @Override
        public boolean hasNextIteration(long it) {
            return false;
        }

        @Override
        public long nextEntry(long it) {
            throw new NoSuchElementException();
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
        public @UnmodifiableView R2OMap<K, V> view() {
            return this;
        }
    }

    class Singleton<K, V> extends Reference2ObjectMaps.Singleton<K, V> implements R2OMap<K, V> {

        protected Singleton(K key, V value) {
            super(key, value);
        }

        @Override
        public long beginIteration() {
            return 1;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public K getIterationKey(long it) {
            if (it != 1) {
                throw new NoSuchElementException();
            }
            return this.key;
        }

        @Override
        public V getIterationValue(long it) {
            if (it != 1) {
                throw new NoSuchElementException();
            }
            return this.value;
        }

        @Override
        public K getSampleKey() {
            return this.key;
        }

        @Override
        public V getSampleValue() {
            return this.value;
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
        public @UnmodifiableView R2OMap<K, V> view() {
            return this;
        }
    }

    class View<K, V> extends Reference2ObjectMaps.UnmodifiableMap<K, V> implements R2OMap<K, V> {

        protected final R2OMap<K, V> m;

        public View(R2OMap<K, V> m) {
            super(m);
            this.m = m;
        }

        @Override
        public long beginIteration() {
            return this.m.beginIteration();
        }

        @Override
        public K getIterationKey(long it) {
            return this.m.getIterationKey(it);
        }

        @Override
        public V getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public K getSampleKey() {
            return this.m.getSampleKey();
        }

        @Override
        public V getSampleValue() {
            return this.m.getSampleValue();
        }

        @Override
        public boolean hasNextIteration(long it) {
            return this.m.hasNextIteration(it);
        }

        @Override
        public long nextEntry(long it) {
            return this.m.nextEntry(it);
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
        public @UnmodifiableView R2OMap<K, V> view() {
            return this;
        }
    }
}
