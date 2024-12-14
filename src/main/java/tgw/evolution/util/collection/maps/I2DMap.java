package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface I2DMap extends Int2DoubleMap, MapExtension {

    long beginIteration();

    @Override
    void clear();

    int getIterationKey(long it);

    double getIterationValue(long it);

    int getSampleKey();

    double getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView
    I2DMap view();

    class EmptyMap extends Int2DoubleMaps.EmptyMap implements I2DMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public int getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public double getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public int getSampleKey() {
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
        public @UnmodifiableView I2DMap view() {
            return this;
        }
    }

    class Singleton extends Int2DoubleMaps.Singleton implements I2DMap {

        protected Singleton(int key, double value) {
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
        public double getIterationValue(long it) {
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
        public @UnmodifiableView I2DMap view() {
            return this;
        }
    }

    class View extends Int2DoubleMaps.UnmodifiableMap implements I2DMap {

        protected final I2DMap m;

        public View(I2DMap m) {
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
        public double getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public int getSampleKey() {
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
        public @UnmodifiableView I2DMap view() {
            return this;
        }
    }
}
