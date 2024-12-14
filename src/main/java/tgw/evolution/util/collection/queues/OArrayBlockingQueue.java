package tgw.evolution.util.collection.queues;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import tgw.evolution.util.collection.lists.OList;

import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;

public class OArrayBlockingQueue<K> implements OQueue<K> {

    protected K[] array;
    protected int end;
    protected int length;
    protected final ReentrantLock lock = new ReentrantLock(true);
    protected int start;

    public OArrayBlockingQueue(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Initial capacity (" + capacity + ") is negative");
        }
        this.array = (K[]) new Object[Math.max(1, capacity)];
        this.length = this.array.length;
    }

    public OArrayBlockingQueue() {
        this(4);
    }

    @Override
    public long beginIteration() {
        ReentrantLock lock = this.lock;
        lock.lock();
        if (this.isEmpty()) {
            lock.unlock();
            return 0;
        }
        return (long) this.start << 32 | this.size();
    }

    @Override
    public void clear() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.start = 0;
            this.end = 0;
            java.util.Arrays.fill(this.array, null);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public K dequeue() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
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
        finally {
            lock.unlock();
        }
    }

    @Override
    public K dequeueLast() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
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
        finally {
            lock.unlock();
        }
    }

    @Override
    public void endIteration() {
        this.lock.unlock();
    }

    @Override
    public void enqueue(K k) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.array[this.end++] = k;
            if (this.end == this.length) {
                this.end = 0;
            }
            if (this.end == this.start) {
                this.expand();
            }
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void enqueueFirst(K k) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.start == 0) {
                this.start = this.length;
            }
            this.array[--this.start] = k;
            if (this.end == this.start) {
                this.expand();
            }
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void enqueueMany(OList<K> list) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
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
        finally {
            lock.unlock();
        }
    }

    protected final void ensureCapacity(int capacity) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (capacity <= this.array.length) {
                return;
            }
            this.array = ObjectArrays.ensureCapacity(this.array, capacity, 0);
            this.length = this.array.length;
        }
        finally {
            lock.unlock();
        }
    }

    protected final void expand() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.resize(this.length, (int) Math.min(Arrays.MAX_ARRAY_SIZE, 2L * this.length));
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public K getIteration(long it) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return this.array[(int) (it >> 32)];
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public boolean hasNextIteration(long it) {
        boolean b = it != 0;
        if (!b) {
            this.lock.unlock();
        }
        return b;
    }

    @Override
    public long nextEntry(long it) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
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
        finally {
            lock.unlock();
        }
    }

    @Override
    public K peek() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.start == this.end) {
                throw new NoSuchElementException();
            }
            return this.array[this.start];
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public K peekLast() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.start == this.end) {
                throw new NoSuchElementException();
            }
            return this.array[(this.end == 0 ? this.length : this.end) - 1];
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public long removeIteration(long it) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
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
        finally {
            lock.unlock();
        }
    }

    protected final void resize(int size, int newLength) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
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
        finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int apparentLength = this.end - this.start;
            return apparentLength >= 0 ? apparentLength : this.length + apparentLength;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void trim() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
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
        finally {
            lock.unlock();
        }
    }
}
