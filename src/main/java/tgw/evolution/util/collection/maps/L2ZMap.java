package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface L2ZMap extends Long2BooleanMap, MapEv {

    long beginIteration();

    @Override
    void clear();

    long getIterationKey(long it);

    boolean getIterationValue(long it);

    long getSampleKey();

    boolean getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView L2ZMap view();

    class EmptyMap extends Long2BooleanMaps.EmptyMap implements L2ZMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public long getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public boolean getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public long getSampleKey() {
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
        public @UnmodifiableView L2ZMap view() {
            return this;
        }
    }

    class Singleton extends Long2BooleanMaps.Singleton implements L2ZMap {

        protected Singleton(long key, boolean value) {
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
        public boolean getIterationValue(long it) {
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
        public @UnmodifiableView L2ZMap view() {
            return this;
        }
    }

    class View extends Long2BooleanMaps.UnmodifiableMap implements L2ZMap {

        protected final L2ZMap m;

        public View(L2ZMap m) {
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
        public boolean getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public long getSampleKey() {
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
        public @UnmodifiableView L2ZMap view() {
            return this;
        }
    }
}
