package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface R2DMap<K> extends Reference2DoubleMap<K>, MapExtension {

    long beginIteration();

    @Override
    void clear();

    K getIterationKey(long it);

    double getIterationValue(long it);

    K getSampleKey();

    double getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView
    R2DMap<K> view();

    class EmptyMap<K> extends Reference2DoubleMaps.EmptyMap<K> implements R2DMap<K> {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public K getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public double getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public K getSampleKey() {
            throw new NoSuchElementException();
        }

        @Override
        public double getSampleValue() {
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
        public @UnmodifiableView R2DMap<K> view() {
            return this;
        }
    }

    class Singleton<K> extends Reference2DoubleMaps.Singleton<K> implements R2DMap<K> {

        protected Singleton(K key, double value) {
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
        public double getIterationValue(long it) {
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
        public double getSampleValue() {
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
        public @UnmodifiableView R2DMap<K> view() {
            return this;
        }
    }

    class View<K> extends Reference2DoubleMaps.UnmodifiableMap<K> implements R2DMap<K> {

        protected final R2DMap<K> m;

        public View(R2DMap<K> m) {
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
        public double getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public K getSampleKey() {
            return this.m.getSampleKey();
        }

        @Override
        public double getSampleValue() {
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
        public @UnmodifiableView R2DMap<K> view() {
            return this;
        }
    }
}
