package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface L2FMap extends Long2FloatMap, MapExtension {

    long beginIteration();

    @Override
    void clear();

    long getIterationKey(long it);

    float getIterationValue(long it);

    long getSampleKey();

    float getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView
    L2FMap view();

    class EmptyMap extends Long2FloatMaps.EmptyMap implements L2FMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public long getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public float getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public long getSampleKey() {
            throw new NoSuchElementException();
        }

        @Override
        public float getSampleValue() {
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
        public @UnmodifiableView L2FMap view() {
            return this;
        }
    }

    class Singleton extends Long2FloatMaps.Singleton implements L2FMap {

        protected Singleton(long key, float value) {
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
        public long getIterationKey(long it) {
            if (it != 1) {
                throw new NoSuchElementException();
            }
            return this.key;
        }

        @Override
        public float getIterationValue(long it) {
            if (it != 1) {
                throw new NoSuchElementException();
            }
            return this.value;
        }

        @Override
        public long getSampleKey() {
            return this.key;
        }

        @Override
        public float getSampleValue() {
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
        public @UnmodifiableView L2FMap view() {
            return this;
        }
    }

    class View extends Long2FloatMaps.UnmodifiableMap implements L2FMap {

        protected final L2FMap m;

        public View(L2FMap m) {
            super(m);
            this.m = m;
        }

        @Override
        public long beginIteration() {
            return this.m.beginIteration();
        }

        @Override
        public long getIterationKey(long it) {
            return this.m.getIterationKey(it);
        }

        @Override
        public float getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public long getSampleKey() {
            return this.m.getSampleKey();
        }

        @Override
        public float getSampleValue() {
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
        public @UnmodifiableView L2FMap view() {
            return this;
        }
    }
}
