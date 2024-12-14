package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public interface L2OMap<V> extends Long2ObjectMap<V>, MapExtension {

    long beginIteration();

    @Override
    void clear();

    long getIterationKey(long it);

    V getIterationValue(long it);

    long getSampleKey();

    V getSampleValue();

    long nextEntry(long it);

    default void preAllocate(int extraSize) {

    }

    default void putAll(L2OMap<? extends V> map) {
        this.preAllocate(map.size());
        for (long it = map.beginIteration(); map.hasNextIteration(it); it = map.nextEntry(it)) {
            this.put(map.getIterationKey(it), map.getIterationValue(it));
        }
    }

    @Override
    default void putAll(@NotNull Map<? extends Long, ? extends V> m) {
        if (m instanceof L2OMap map) {
            this.putAll(map);
            return;
        }
        this.preAllocate(m.size());
        if (m instanceof Long2ObjectMap) {
            ObjectIterator<Long2ObjectMap.Entry<V>> i = Long2ObjectMaps.fastIterator((Long2ObjectMap) m);
            while (i.hasNext()) {
                Long2ObjectMap.Entry<? extends V> e = i.next();
                this.put(e.getKey(), e.getValue());
            }
        }
        else {
            int n = m.size();
            Iterator<? extends Map.Entry<? extends Long, ? extends V>> i = m.entrySet().iterator();
            while (n-- != 0) {
                Map.Entry<? extends Long, ? extends V> e = i.next();
                this.put(e.getKey(), e.getValue());
            }
        }
    }

    long removeIteration(long it);

    @UnmodifiableView
    L2OMap<V> view();

    class EmptyMap<V> extends Long2ObjectMaps.EmptyMap<V> implements L2OMap<V> {

        @Override
        public long beginIteration() {
            return 0;
        }

        @Override
        public long getIterationKey(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public V getIterationValue(long it) {
            throw new NoSuchElementException();
        }

        @Override
        public long getSampleKey() {
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
        public void putAll(Map<? extends Long, ? extends V> m) {
            throw new UnsupportedOperationException();
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
        public @UnmodifiableView L2OMap<V> view() {
            return this;
        }
    }

    class Singleton<V> extends Long2ObjectMaps.Singleton<V> implements L2OMap<V> {

        protected Singleton(long key, V value) {
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
        public V getIterationValue(long it) {
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
        public V getSampleValue() {
            return this.value;
        }

        @Override
        public long nextEntry(long it) {
            return 0;
        }

        @Override
        public void putAll(Map<? extends Long, ? extends V> m) {
            throw new UnsupportedOperationException();
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
        public @UnmodifiableView L2OMap<V> view() {
            return this;
        }
    }

    class View<V> extends Long2ObjectMaps.UnmodifiableMap<V> implements L2OMap<V> {

        protected final L2OMap<V> m;

        public View(L2OMap<V> m) {
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
        public V getIterationValue(long it) {
            return this.m.getIterationValue(it);
        }

        @Override
        public long getSampleKey() {
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
        public void putAll(Map<? extends Long, ? extends V> m) {
            throw new UnsupportedOperationException();
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
        public @UnmodifiableView L2OMap<V> view() {
            return this;
        }
    }
}
