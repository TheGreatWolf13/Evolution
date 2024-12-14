package tgw.evolution.util.collection.queues;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.ints.IntArrays;
import tgw.evolution.util.collection.lists.IList;

import java.util.NoSuchElementException;

public class IArrayQueue implements IQueue {

    protected int[] array;
    protected int end;
    protected int length;
    protected int start;

    public IArrayQueue(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Initial capacity (" + capacity + ") is negative");
        }
        this.array = new int[Math.max(1, capacity)];
        this.length = this.array.length;
    }

    public IArrayQueue() {
        this(4);
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
    }

    @Override
    public int dequeue() {
        if (this.start == this.end) {
            throw new NoSuchElementException();
        }
        int t = this.array[this.start];
        if (++this.start == this.length) {
            this.start = 0;
        }
        return t;
    }

    @Override
    public int dequeueLast() {
        if (this.start == this.end) {
            throw new NoSuchElementException();
        }
        if (this.end == 0) {
            this.end = this.length;
        }
        return this.array[--this.end];
    }

    @Override
    public void endIteration() {
        //Nothing to do
    }

    @Override
    public void enqueue(int k) {
        this.array[this.end++] = k;
        if (this.end == this.length) {
            this.end = 0;
        }
        if (this.end == this.start) {
            this.expand();
        }
    }

    @Override
    public void enqueueFirst(int k) {
        if (this.start == 0) {
            this.start = this.length;
        }
        this.array[--this.start] = k;
        if (this.end == this.start) {
            this.expand();
        }
    }

    @Override
    public void enqueueMany(IList list) {
        if (!this.isEmpty()) {
            int size = this.size();
            int needed = list.size();
            if (size + needed + 1 > this.length) {
                this.resize(this.length, Math.min(Arrays.MAX_ARRAY_SIZE, size + needed + 1));
            }
            int slotsAtEnd = Math.min(this.length - this.end, needed);
            list.getElements(0, this.array, this.end, slotsAtEnd);
            needed -= slotsAtEnd;
            this.end += slotsAtEnd;
            if (this.end == this.length) {
                this.end = 0;
            }
            if (needed > 0) {
                list.getElements(slotsAtEnd, this.array, 0, needed);
                this.end += needed;
            }
        }
        else {
            int needed = list.size();
            this.ensureCapacity(needed + 1);
            this.start = 0;
            this.end = needed;
            list.getElements(0, this.array, 0, needed);
        }
    }

    protected final void ensureCapacity(int capacity) {
        if (capacity <= this.array.length) {
            return;
        }
        this.array = IntArrays.ensureCapacity(this.array, capacity, 0);
        this.length = this.array.length;
    }

    protected final void expand() {
        this.resize(this.length, (int) Math.min(Arrays.MAX_ARRAY_SIZE, 2L * this.length));
    }

    @Override
    public int getIteration(long it) {
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
    public int peek() {
        if (this.start == this.end) {
            throw new NoSuchElementException();
        }
        return this.array[this.start];
    }

    @Override
    public int peekLast() {
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
            return it;
        }
        assert this.end - pos > 0;
        System.arraycopy(this.array, pos + 1, this.array, pos, this.end - pos);
        --this.end;
        int size = (int) it;
        return (long) (pos - 1) << 32 | size;
    }

    protected final void resize(int size, int newLength) {
        int[] newArray = new int[newLength];
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
        int[] newArray = new int[size + 1];
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
