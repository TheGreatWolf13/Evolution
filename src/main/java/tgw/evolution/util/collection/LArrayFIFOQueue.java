package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;

import java.util.NoSuchElementException;

public class LArrayFIFOQueue extends LongArrayFIFOQueue {

    @Override
    public long dequeueLastLong() {
        if (this.start == this.end) {
            throw new NoSuchElementException();
        }
        if (this.end == 0) {
            this.end = this.length;
        }
        return this.array[--this.end];
    }

    @Override
    public long dequeueLong() {
        if (this.start == this.end) {
            throw new NoSuchElementException();
        }
        long t = this.array[this.start];
        if (++this.start == this.length) {
            this.start = 0;
        }
        return t;
    }

    public long getDirect(int index) {
        if (this.start == this.end) {
            throw new NoSuchElementException();
        }
        if (index >= this.size()) {
            throw new IndexOutOfBoundsException(index + " >= " + this.size());
        }
        int localIndex = index + this.start;
        if (localIndex >= this.length) {
            localIndex -= this.length;
        }
        return this.array[localIndex];
    }
}
