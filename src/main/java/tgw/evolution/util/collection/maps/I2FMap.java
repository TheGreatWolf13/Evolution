package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface I2FMap extends Int2FloatMap, MapExtension {

    long beginIteration();

    @Override
    void clear();

    int getIterationKey(long it);

    float getIterationValue(long it);

    int getSampleKey();

    float getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView
    I2FMap view();

    class EmptyMap extends Int2FloatMaps.EmptyMap implements I2FMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public int getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public float getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public int getSampleKey() {
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
        public @UnmodifiableView I2FMap view() {
            return this;
        }
    }

    class Singleton extends Int2FloatMaps.Singleton implements I2FMap {

        protected Singleton(int key, float value) {
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
        public int getIterationKey(long it) {
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
        public int getSampleKey() {
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
        public @UnmodifiableView I2FMap view() {
            return this;
        }
    }

    class View extends Int2FloatMaps.UnmodifiableMap implements I2FMap {

        protected final I2FMap m;

        public View(I2FMap m) {
            super(m);
            this.m = m;
        }

        @Override
        public long beginIteration() {
            return this.m.beginIteration();
        }

        @Override
        public int getIterationKey(long it) {
            return this.m.getIterationKey(it);
        }

        @Override
        public float getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public int getSampleKey() {
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
        public @UnmodifiableView I2FMap view() {
            return this;
        }
    }
}
