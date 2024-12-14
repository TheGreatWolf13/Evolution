package tgw.evolution.util.collection.queues;

import it.unimi.dsi.fastutil.Arrays;
import tgw.evolution.util.collection.lists.OList;

import java.util.NoSuchElementException;

public class OArrayLimitedQueue<K> implements OQueue<K> {

    protected K[] array;
    protected int end;
    protected int length;
    protected final int limit;
    protected int start;

    public OArrayLimitedQueue(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit (" + limit + ") is negative or zero");
        }
        if (limit + 1 > Arrays.MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException("Limit (" + limit + ") is too big");
        }
        this.array = (K[]) new Object[Math.min(4, limit + 1)];
        this.limit = limit;
        this.length = this.array.length;
    }

    @Override
    public long beginIteration() {
        if (this.isEmpty()) {
            return 0;
        }
        return (long) this.start << 32 | this.size();
    }

    @Override
    public void clear() {
        this.start = 0;
        this.end = 0;
        java.util.Arrays.fill(this.array, null);
    }

    @Override
    public K dequeue() {
        if (this.start == this.end) {
            throw new NoSuchElementException();
        }
        K t = this.array[this.start];
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
        K t = this.array[--this.end];
        this.array[this.end] = null;
        return t;
    }

    @Override
    public void endIteration() {
        //Nothing to do
    }

    @Override
    public void enqueue(K k) {
        if (this.size() == this.limit) {
            this.dequeue();
        }
        this.array[this.end++] = k;
        if (this.end == this.length) {
            this.end = 0;
        }
        if (this.end == this.start) {
            this.expand();
        }
    }

    @Override
    public void enqueueFirst(K k) {
        if (this.size() == this.limit) {
            this.dequeue();
        }
        if (this.start == 0) {
            this.start = this.length;
        }
        this.array[--this.start] = k;
        if (this.end == this.start) {
            this.expand();
        }
    }

    @Override
    public void enqueueMany(OList<K> list) {
        //Sorry I am lazy to optimize this for now
        for (int i = 0, len = list.size(); i < len; ++i) {
            this.enqueue(list.get(i));
        }
    }

    protected final void expand() {
        this.resize(this.length, (int) Math.min(Arrays.MAX_ARRAY_SIZE, 2L * this.length));
    }

    @Override
    public K getIteration(long it) {
        return this.array[(int) (it >> 32)];
    }

    @Override
    public long nextEntry(long it) {
        if (this.isEmpty()) {
            return 0;
        }
        int size = (int) it;
        if (--size == 0) {
            return 0;
        }
        int pos = (int) (it >> 32) + 1;
        if (pos == this.length) {
            pos = 0;
        }
        return (long) pos << 32 | size;
    }

    @Override
    public K peek() {
        if (this.start == this.end) {
            throw new NoSuchElementException();
        }
        return this.array[this.start];
    }

    @Override
    public K peekLast() {
        if (this.start == this.end) {
            throw new NoSuchElementException();
        }
        return this.array[(this.end == 0 ? this.length : this.end) - 1];
    }

    @Override
    public long removeIteration(long it) {
        int pos = (int) (it >> 32);
        if (pos == this.start) {
            this.dequeue();
            return it;
        }
        int realEnd = this.end;
        if (realEnd == 0) {
            realEnd = this.length;
        }
        if (pos == realEnd - 1) {
            this.dequeueLast();
            return it;
        }
        if (pos - this.start > 0) {
            System.arraycopy(this.array, this.start, this.array, this.start + 1, pos - this.start);
            this.array[this.start++] = null;
            return it;
        }
        assert this.end - pos > 0;
        System.arraycopy(this.array, pos + 1, this.array, pos, this.end - pos);
        --this.end;
        int size = (int) it;
        return (long) (pos - 1) << 32 | size;
    }

    protected final void resize(int size, int newLength) {
        if (newLength > this.limit + 1) {
            newLength = this.limit + 1;
        }
        K[] newArray = (K[]) new Object[newLength];
        if (this.start >= this.end) {
            if (size != 0) {
                System.arraycopy(this.array, this.start, newArray, 0, this.length - this.start);
                System.arraycopy(this.array, 0, newArray, this.length - this.start, this.end);
            }
        }
        else {
            System.arraycopy(this.array, this.start, newArray, 0, this.end - this.start);
        }
        this.start = 0;
        this.end = size;
        this.array = newArray;
        this.length = newLength;
    }

    @Override
    public int size() {
        int apparentLength = this.end - this.start;
        return apparentLength >= 0 ? apparentLength : this.length + apparentLength;
    }

    @Override
    public void trim() {
        int size = this.size();
        if (size + 1 == this.length) {
            return;
        }
        K[] newArray = (K[]) new Object[size + 1];
        if (this.start <= this.end) {
            System.arraycopy(this.array, this.start, newArray, 0, this.end - this.start);
        }
        else {
            System.arraycopy(this.array, this.start, newArray, 0, this.length - this.start);
            System.arraycopy(this.array, 0, newArray, this.length - this.start, this.end);
        }
        this.start = 0;
        this.length = (this.end = size) + 1;
        this.array = newArray;
    }
}
