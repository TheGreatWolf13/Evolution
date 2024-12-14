package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface B2OMap<V> extends Byte2ObjectMap<V>, MapExtension {

    long beginIteration();

    @Override
    void clear();

    byte getIterationKey(long it);

    V getIterationValue(long it);

    byte getSampleKey();

    V getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView
    B2OMap<V> view();

    class EmptyMap<V> extends Byte2ObjectMaps.EmptyMap<V> implements B2OMap<V> {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public byte getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public V getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public byte getSampleKey() {
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
        public @UnmodifiableView B2OMap<V> view() {
            return this;
        }
    }

    class Singleton<V> extends Byte2ObjectMaps.Singleton<V> implements B2OMap<V> {

        protected Singleton(byte key, V value) {
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
        public V getIterationValue(long it) {
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
        public @UnmodifiableView B2OMap<V> view() {
            return this;
        }
    }

    class View<V> extends Byte2ObjectMaps.UnmodifiableMap<V> implements B2OMap<V> {

        protected final B2OMap<V> m;

        public View(B2OMap<V> m) {
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
        public V getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public byte getSampleKey() {
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
        public @UnmodifiableView B2OMap<V> view() {
            return this;
        }
    }
}
