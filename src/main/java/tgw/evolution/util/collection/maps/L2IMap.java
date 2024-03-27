package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface L2IMap extends Long2IntMap, MapEv {

    long beginIteration();

    @Override
    void clear();

    long getIterationKey(long it);

    int getIterationValue(long it);

    long getSampleKey();

    int getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView L2IMap view();

    class EmptyMap extends Long2IntMaps.EmptyMap implements L2IMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public long getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public int getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public long getSampleKey() {
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
        public @UnmodifiableView L2IMap view() {
            return this;
        }
    }

    class Singleton extends Long2IntMaps.Singleton implements L2IMap {

        protected Singleton(long key, int value) {
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
        public long getIterationKey(long it) {
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
        public long getSampleKey() {
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
        public @UnmodifiableView L2IMap view() {
            return this;
        }
    }

    class View extends Long2IntMaps.UnmodifiableMap implements L2IMap {

        protected final L2IMap m;

        public View(L2IMap m) {
            super(m);
            this.m = m;
        }

        @Override
        public long beginIteration() {
            return this.m.beginIteration();
        }

        @Override
        public long getIterationKey(long it) {
            return this.m.getIterationKey(it);
        }

        @Override
        public int getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public long getSampleKey() {
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
        public @UnmodifiableView L2IMap view() {
            return this;
        }
    }
}
