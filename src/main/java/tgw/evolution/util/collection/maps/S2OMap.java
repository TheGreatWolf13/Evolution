package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface S2OMap<V> extends Short2ObjectMap<V>, MapExtension {

    long beginIteration();

    @Override
    void clear();

    short getIterationKey(long it);

    V getIterationValue(long it);

    short getSampleKey();

    V getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView
    S2OMap<V> view();

    class EmptyMap<V> extends Short2ObjectMaps.EmptyMap<V> implements S2OMap<V> {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public short getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public V getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public short getSampleKey() {
            throw new NoSuchElementException();
        }

        @Override
        public V getSampleValue() {
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
        public @UnmodifiableView S2OMap<V> view() {
            return this;
        }
    }

    class Singleton<V> extends Short2ObjectMaps.Singleton<V> implements S2OMap<V> {

        protected Singleton(short key, V value) {
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
        public V getIterationValue(long it) {
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
        public V getSampleValue() {
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
        public @UnmodifiableView S2OMap view() {
            return this;
        }
    }

    class View<V> extends Short2ObjectMaps.UnmodifiableMap<V> implements S2OMap<V> {

        protected final S2OMap<V> m;

        public View(S2OMap<V> m) {
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
        public V getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public short getSampleKey() {
            return this.m.getSampleKey();
        }

        @Override
        public V getSampleValue() {
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
        public @UnmodifiableView S2OMap<V> view() {
            return this;
        }
    }
}
