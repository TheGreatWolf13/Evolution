package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import it.unimi.dsi.fastutil.ints.Int2ByteMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface I2BMap extends Int2ByteMap, MapEv {

    long beginIteration();

    @Override
    void clear();

    int getIterationKey(long it);

    byte getIterationValue(long it);

    int getSampleKey();

    byte getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView I2BMap view();

    class EmptyMap extends Int2ByteMaps.EmptyMap implements I2BMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public int getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public byte getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public int getSampleKey() {
            throw new NoSuchElementException();
        }

        @Override
        public byte getSampleValue() {
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
        public @UnmodifiableView I2BMap view() {
            return this;
        }
    }

    class Singleton extends Int2ByteMaps.Singleton implements I2BMap {

        protected Singleton(int key, byte value) {
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
        public byte getIterationValue(long it) {
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
        public byte getSampleValue() {
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
        public @UnmodifiableView I2BMap view() {
            return this;
        }
    }

    class View extends Int2ByteMaps.UnmodifiableMap implements I2BMap {

        protected final I2BMap m;

        public View(I2BMap m) {
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
        public byte getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public int getSampleKey() {
            return this.m.getSampleKey();
        }

        @Override
        public byte getSampleValue() {
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
        public @UnmodifiableView I2BMap view() {
            return this;
        }
    }
}
