package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.shorts.Short2LongMap;
import it.unimi.dsi.fastutil.shorts.Short2LongMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface S2LMap extends Short2LongMap, MapExtension {

    long beginIteration();

    @Override
    void clear();

    short getIterationKey(long it);

    long getIterationValue(long it);

    short getSampleKey();

    long getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView
    S2LMap view();

    class EmptyMap extends Short2LongMaps.EmptyMap implements S2LMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public short getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public long getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public short getSampleKey() {
            throw new NoSuchElementException();
        }

        @Override
        public long getSampleValue() {
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
        public @UnmodifiableView S2LMap view() {
            return this;
        }
    }

    class Singleton extends Short2LongMaps.Singleton implements S2LMap {

        protected Singleton(short key, long value) {
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
        public long getIterationValue(long it) {
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
        public long getSampleValue() {
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
        public @UnmodifiableView S2LMap view() {
            return this;
        }
    }

    class View extends Short2LongMaps.UnmodifiableMap implements S2LMap {

        protected final S2LMap m;

        public View(S2LMap m) {
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
        public long getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public short getSampleKey() {
            return this.m.getSampleKey();
        }

        @Override
        public long getSampleValue() {
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
        public @UnmodifiableView S2LMap view() {
            return this;
        }
    }
}
