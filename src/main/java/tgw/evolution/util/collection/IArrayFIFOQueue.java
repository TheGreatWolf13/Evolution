package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;

import java.util.NoSuchElementException;

public class IArrayFIFOQueue extends IntArrayFIFOQueue {

    @Override
    public int dequeueInt() {
        if (this.start == this.end) {
            throw new NoSuchElementException();
        }
        final int t = this.array[this.start];
        if (++this.start == this.length) {
            this.start = 0;
        }
        return t;
    }

    @Override
    public int dequeueLastInt() {
        if (this.start == this.end) {
            throw new NoSuchElementException();
        }
        if (this.end == 0) {
            this.end = this.length;
        }
        return this.array[--this.end];
    }
}
