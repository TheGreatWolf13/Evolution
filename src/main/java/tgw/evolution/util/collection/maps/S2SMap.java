package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.shorts.Short2ShortMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface S2SMap extends Short2ShortMap, MapEv {

    long beginIteration();

    @Override
    void clear();

    short getIterationKey(long it);

    short getIterationValue(long it);

    short getSampleKey();

    short getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView S2SMap view();

    class EmptyMap extends Short2ShortMaps.EmptyMap implements S2SMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public short getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public short getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public short getSampleKey() {
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
        public @UnmodifiableView S2SMap view() {
            return this;
        }
    }

    class Singleton extends Short2ShortMaps.Singleton implements S2SMap {

        protected Singleton(short key, short value) {
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
        public short getIterationValue(long it) {
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
        public @UnmodifiableView S2SMap view() {
            return this;
        }
    }

    class View extends Short2ShortMaps.UnmodifiableMap implements S2SMap {

        protected final S2SMap m;

        public View(S2SMap m) {
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
        public short getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public short getSampleKey() {
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
        public @UnmodifiableView S2SMap view() {
            return this;
        }
    }
}
