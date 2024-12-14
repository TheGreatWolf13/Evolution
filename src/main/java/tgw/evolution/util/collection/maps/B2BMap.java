package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.bytes.Byte2ByteMap;
import it.unimi.dsi.fastutil.bytes.Byte2ByteMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface B2BMap extends Byte2ByteMap, MapExtension {

    long beginIteration();

    @Override
    void clear();

    byte getIterationKey(long it);

    byte getIterationValue(long it);

    byte getSampleKey();

    byte getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView
    B2BMap view();

    class EmptyMap extends Byte2ByteMaps.EmptyMap implements B2BMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public byte getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public byte getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public byte getSampleKey() {
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
        public @UnmodifiableView B2BMap view() {
            return this;
        }
    }

    class Singleton extends Byte2ByteMaps.Singleton implements B2BMap {

        protected Singleton(byte key, byte value) {
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
        public byte getIterationKey(long it) {
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
        public byte getSampleKey() {
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
        public @UnmodifiableView B2BMap view() {
            return this;
        }
    }

    class View extends Byte2ByteMaps.UnmodifiableMap implements B2BMap {

        protected final B2BMap m;

        public View(B2BMap m) {
            super(m);
            this.m = m;
        }

        @Override
        public long beginIteration() {
            return this.m.beginIteration();
        }

        @Override
        public byte getIterationKey(long it) {
            return this.m.getIterationKey(it);
        }

        @Override
        public byte getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public byte getSampleKey() {
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
        public @UnmodifiableView B2BMap view() {
            return this;
        }
    }
}
