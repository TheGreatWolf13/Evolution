package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.NoSuchElementException;

public class OArrayFIFOQueue<K> extends ObjectArrayFIFOQueue<K> {

    @Override
    public K dequeue() {
        if (this.start == this.end) {
            throw new NoSuchElementException();
        }
        final K t = this.array[this.start];
        this.array[this.start] = null;
        if (++this.start == this.length) {
            this.start = 0;
        }
        return t;
    }

    @Override
    public K dequeueLast() {
        if (this.start == this.end) {
            throw new NoSuchElementException();
        }
        if (this.end == 0) {
            this.end = this.length;
        }
        final K t = this.array[--this.end];
        this.array[this.end] = null;
        return t;
    }

    public void enqueueMany(ObjectList<K> list) {
        if (!this.isEmpty()) {
            throw new IllegalStateException("Can only enqueue many while empty!");
        }
        int size = list.size();
        this.ensureCapacity(size);
        this.start = 0;
        this.end = size;
        list.getElements(0, this.array, 0, size);
    }

    private void ensureCapacity(final int capacity) {
        if (capacity <= this.array.length) {
            return;
        }
        this.array = ObjectArrays.ensureCapacity(this.array, capacity, 0);
    }
}
