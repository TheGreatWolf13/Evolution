package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.shorts.Short2FloatMap;
import it.unimi.dsi.fastutil.shorts.Short2FloatMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface S2FMap extends Short2FloatMap, MapEv {

    long beginIteration();

    @Override
    void clear();

    short getIterationKey(long it);

    float getIterationValue(long it);

    short getSampleKey();

    float getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView S2FMap view();

    class EmptyMap extends Short2FloatMaps.EmptyMap implements S2FMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public short getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public float getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public short getSampleKey() {
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
        public @UnmodifiableView S2FMap view() {
            return this;
        }
    }

    class Singleton extends Short2FloatMaps.Singleton implements S2FMap {

        protected Singleton(short key, float value) {
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
        public short getIterationKey(long it) {
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
        public short getSampleKey() {
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
        public @UnmodifiableView S2FMap view() {
            return this;
        }
    }

    class View extends Short2FloatMaps.UnmodifiableMap implements S2FMap {

        protected final S2FMap m;

        public View(S2FMap m) {
            super(m);
            this.m = m;
        }

        @Override
        public long beginIteration() {
            return this.m.beginIteration();
        }

        @Override
        public short getIterationKey(long it) {
            return this.m.getIterationKey(it);
        }

        @Override
        public float getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public short getSampleKey() {
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
        public @UnmodifiableView S2FMap view() {
            return this;
        }
    }
}
