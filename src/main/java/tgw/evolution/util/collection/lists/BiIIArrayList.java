package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.ints.IntArrays;

/**
 * A custom implementation of an {@link java.util.ArrayList ArrayList} used to store two primitive integers instead of one.
 * An advantage of this implementation is that it does not require a 'holder' object to be created and does not require Boxing and Unboxing.
 */
public class BiIIArrayList {

    /**
     * The backing array of the left elements.
     */
    protected int[] a;
    /**
     * The backing array of the right elements.
     */
    protected int[] b;
    /**
     * The current actual size of the list (never greater than the backing-array
     * length).
     */
    protected int size;

    public BiIIArrayList() {
        this.a = IntArrays.DEFAULT_EMPTY_ARRAY; // We delay allocation
        this.b = IntArrays.DEFAULT_EMPTY_ARRAY; // We delay allocation
    }

    public void add(final int l, final int r) {
        this.grow(this.size + 1);
        this.a[this.size] = l;
        this.b[this.size++] = r;
        assert this.size <= this.a.length;
        assert this.size <= this.b.length;
    }

    public void clear() {
        this.size = 0;
    }

    public boolean contains(final int l, final int r, final int fromIndex) {
        if (fromIndex >= this.size) {
            throw new IndexOutOfBoundsException("Index (" + fromIndex + ") is greater than or equal to list size (" + this.size + ")");
        }
        for (int i = fromIndex; i < this.size; i++) {
            if (this.a[i] == l && this.b[i] == r) {
                return true;
            }
        }
        return false;
    }

    public int getLeft(final int index) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + this.size + ")");
        }
        return this.a[index];
    }

    public int getRight(final int index) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + this.size + ")");
        }
        return this.b[index];
    }

    /**
     * Grows this array list, ensuring that it can contain the given number of
     * entries without resizing, and in case increasing the current capacity at
     * least by a factor of 50%.
     *
     * @param capacity the new minimum capacity for this array list.
     */
    private void grow(int capacity) {
        if (capacity <= this.a.length) {
            return;
        }
        //noinspection ArrayEquality
        if (this.a != IntArrays.DEFAULT_EMPTY_ARRAY) {
            capacity = (int) Math.max(Math.min((long) this.a.length + (this.a.length >> 1), Arrays.MAX_ARRAY_SIZE), capacity);
        }
        else if (capacity < 10) {
            capacity = 10;
        }
        this.a = IntArrays.forceCapacity(this.a, capacity, this.size);
        this.b = IntArrays.forceCapacity(this.b, capacity, this.size);
        assert this.size <= this.a.length;
        assert this.size <= this.b.length;
    }

    public int indexOfLeft(final int k) {
        for (int i = 0; i < this.size; i++) {
            if (k == this.a[i]) {
                return i;
            }
        }
        return -1;
    }

    public void removeAt(final int index) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + this.size + ")");
        }
        this.size--;
        if (index != this.size) {
            System.arraycopy(this.a, index + 1, this.a, index, this.size - index);
            System.arraycopy(this.b, index + 1, this.b, index, this.size - index);
        }
        assert this.size <= this.a.length;
        assert this.size <= this.b.length;
    }

    public int size() {
        return this.size;
    }

    public void trim(final int n) {
        if (n >= this.a.length || this.size == this.a.length) {
            return;
        }
        int newSize = Math.max(n, this.size);
        final int[] t = new int[newSize];
        System.arraycopy(this.a, 0, t, 0, this.size);
        this.a = t;
        final int[] u = new int[newSize];
        System.arraycopy(this.b, 0, u, 0, this.size);
        this.b = u;
        assert this.size <= this.a.length;
        assert this.size <= this.b.length;
    }

    /**
     * Trims this array list so that the capacity is equal to the size.
     *
     * @see java.util.ArrayList#trimToSize()
     */
    public void trim() {
        this.trim(0);
    }
}
