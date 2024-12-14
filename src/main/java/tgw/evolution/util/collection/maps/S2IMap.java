package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.shorts.Short2IntMap;
import it.unimi.dsi.fastutil.shorts.Short2IntMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface S2IMap extends Short2IntMap, MapExtension {

    long beginIteration();

    @Override
    void clear();

    short getIterationKey(long it);

    int getIterationValue(long it);

    short getSampleKey();

    int getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView
    S2IMap view();

    class EmptyMap extends Short2IntMaps.EmptyMap implements S2IMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public short getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public int getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public short getSampleKey() {
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
        public @UnmodifiableView S2IMap view() {
            return this;
        }
    }

    class Singleton extends Short2IntMaps.Singleton implements S2IMap {

        protected Singleton(short key, int value) {
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
        public int getIterationValue(long it) {
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
        public @UnmodifiableView S2IMap view() {
            return this;
        }
    }

    class View extends Short2IntMaps.UnmodifiableMap implements S2IMap {

        protected final S2IMap m;

        public View(S2IMap m) {
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
        public int getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public short getSampleKey() {
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
        public @UnmodifiableView S2IMap view() {
            return this;
        }
    }
}
