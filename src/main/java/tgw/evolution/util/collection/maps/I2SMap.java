package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface I2SMap extends Int2ShortMap, MapExtension {

    long beginIteration();

    @Override
    void clear();

    int getIterationKey(long it);

    short getIterationValue(long it);

    int getSampleKey();

    short getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView
    I2SMap view();

    class EmptyMap extends Int2ShortMaps.EmptyMap implements I2SMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public int getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public short getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public int getSampleKey() {
            throw new NoSuchElementException();
        }

        @Override
        public short getSampleValue() {
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
        public @UnmodifiableView I2SMap view() {
            return this;
        }
    }

    class Singleton extends Int2ShortMaps.Singleton implements I2SMap {

        protected Singleton(int key, short value) {
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
        public short getIterationValue(long it) {
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
        public short getSampleValue() {
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
        public @UnmodifiableView I2SMap view() {
            return this;
        }
    }

    class View extends Int2ShortMaps.UnmodifiableMap implements I2SMap {

        protected final I2SMap m;

        public View(I2SMap m) {
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
        public short getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public int getSampleKey() {
            return this.m.getSampleKey();
        }

        @Override
        public short getSampleValue() {
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
        public @UnmodifiableView I2SMap view() {
            return this;
        }
    }
}
