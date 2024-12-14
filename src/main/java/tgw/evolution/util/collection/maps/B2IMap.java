package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.bytes.Byte2IntMap;
import it.unimi.dsi.fastutil.bytes.Byte2IntMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface B2IMap extends Byte2IntMap, MapExtension {

    long beginIteration();

    @Override
    void clear();

    byte getIterationKey(long it);

    int getIterationValue(long it);

    byte getSampleKey();

    int getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView
    B2IMap view();

    class EmptyMap extends Byte2IntMaps.EmptyMap implements B2IMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public byte getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public int getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public byte getSampleKey() {
            throw new NoSuchElementException();
        }

        @Override
        public int getSampleValue() {
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
        public @UnmodifiableView B2IMap view() {
            return this;
        }
    }

    class Singleton extends Byte2IntMaps.Singleton implements B2IMap {

        protected Singleton(byte key, int value) {
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
        public int getIterationValue(long it) {
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
        public int getSampleValue() {
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
        public @UnmodifiableView B2IMap view() {
            return this;
        }
    }

    class View extends Byte2IntMaps.UnmodifiableMap implements B2IMap {

        protected final B2IMap m;

        public View(B2IMap m) {
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
        public int getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public byte getSampleKey() {
            return this.m.getSampleKey();
        }

        @Override
        public int getSampleValue() {
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
        public @UnmodifiableView B2IMap view() {
            return this;
        }
    }
}
