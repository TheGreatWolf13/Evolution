package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface I2ZMap extends Int2BooleanMap, MapExtension {

    long beginIteration();

    @Override
    void clear();

    int getIterationKey(long it);

    boolean getIterationValue(long it);

    int getSampleKey();

    boolean getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView
    I2ZMap view();

    class EmptyMap extends Int2BooleanMaps.EmptyMap implements I2ZMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public int getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public boolean getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public int getSampleKey() {
            throw new NoSuchElementException();
        }

        @Override
        public boolean getSampleValue() {
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
        public @UnmodifiableView I2ZMap view() {
            return this;
        }
    }

    class Singleton extends Int2BooleanMaps.Singleton implements I2ZMap {

        protected Singleton(int key, boolean value) {
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
        public boolean getIterationValue(long it) {
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
        public boolean getSampleValue() {
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
        public @UnmodifiableView I2ZMap view() {
            return this;
        }
    }

    class View extends Int2BooleanMaps.UnmodifiableMap implements I2ZMap {

        protected final I2ZMap m;

        public View(I2ZMap m) {
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
        public boolean getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public int getSampleKey() {
            return this.m.getSampleKey();
        }

        @Override
        public boolean getSampleValue() {
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
        public @UnmodifiableView I2ZMap view() {
            return this;
        }
    }
}
