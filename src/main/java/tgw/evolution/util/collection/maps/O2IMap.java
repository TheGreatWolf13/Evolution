package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.NoSuchElementException;

public interface O2IMap<K> extends Object2IntMap<K>, MapExtension {

    long beginIteration();

    @Override
    void clear();

    K getIterationKey(long it);

    int getIterationValue(long it);

    K getSampleKey();

    int getSampleValue();

    long nextEntry(long it);

    long removeIteration(long it);

    @UnmodifiableView
    O2IMap<K> view();

    class EmptyMap<K> extends Object2IntMaps.EmptyMap<K> implements O2IMap<K> {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public K getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public int getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public K getSampleKey() {
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
        public @UnmodifiableView O2IMap<K> view() {
            return this;
        }
    }

    class Singleton<K> extends Object2IntMaps.Singleton<K> implements O2IMap<K> {

        protected Singleton(K key, int value) {
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
        public K getIterationKey(long it) {
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
        public K getSampleKey() {
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
        public @UnmodifiableView O2IMap<K> view() {
            return this;
        }
    }

    class View<K> extends Object2IntMaps.UnmodifiableMap<K> implements O2IMap<K> {

        protected final O2IMap<K> m;

        public View(O2IMap<K> m) {
            super(m);
            this.m = m;
        }

        @Override
        public long beginIteration() {
            return this.m.beginIteration();
        }

        @Override
        public K getIterationKey(long it) {
            return this.m.getIterationKey(it);
        }

        @Override
        public int getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public K getSampleKey() {
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
        public @UnmodifiableView O2IMap<K> view() {
            return this;
        }
    }
}
