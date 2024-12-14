package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.bytes.Byte2BooleanMap;
import it.unimi.dsi.fastutil.bytes.Byte2BooleanMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface B2ZMap extends Byte2BooleanMap, MapExtension {

    long beginIteration();

    @Override
    void clear();

    byte getIterationKey(long it);

    boolean getIterationValue(long it);

    byte getSampleKey();

    boolean getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView
    B2ZMap view();

    class EmptyMap extends Byte2BooleanMaps.EmptyMap implements B2ZMap {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public byte getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public boolean getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public byte getSampleKey() {
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
        public @UnmodifiableView B2ZMap view() {
            return this;
        }
    }

    class Singleton extends Byte2BooleanMaps.Singleton implements B2ZMap {

        protected Singleton(byte key, boolean value) {
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
        public boolean getIterationValue(long it) {
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
        public @UnmodifiableView B2ZMap view() {
            return this;
        }
    }

    class View extends Byte2BooleanMaps.UnmodifiableMap implements B2ZMap {

        protected final B2ZMap m;

        public View(B2ZMap m) {
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
        public boolean getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public byte getSampleKey() {
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
        public @UnmodifiableView B2ZMap view() {
            return this;
        }
    }
}
