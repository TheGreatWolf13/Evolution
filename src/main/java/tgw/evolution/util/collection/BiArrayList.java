package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

import java.util.random.RandomGenerator;

/**
 * A custom implementation of an {@link java.util.ArrayList ArrayList} used to store two objects instead of one.
 * An advantage of this implementation is that it does not require a 'holder' object to be created.
 */
public class BiArrayList<L, R> {

    /**
     * The initial default capacity of an array list.
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 10;
    /**
     * The backing array of the left elements.
     */
    protected transient L[] a;
    /**
     * The backing array of the right elements.
     */
    protected transient R[] b;
    /**
     * The current actual size of the list (never greater than the backing-array
     * length).
     */
    protected int size;

    public BiArrayList() {
        this.a = (L[]) ObjectArrays.DEFAULT_EMPTY_ARRAY; // We delay allocation
        this.b = (R[]) ObjectArrays.DEFAULT_EMPTY_ARRAY; // We delay allocation
    }

    public void add(final L l, final R r) {
        this.grow(this.size + 1);
        this.a[this.size] = l;
        this.b[this.size++] = r;
        assert this.size <= this.a.length;
        assert this.size <= this.b.length;
    }

    public void clear() {
        java.util.Arrays.fill(this.a, 0, this.size, null);
        java.util.Arrays.fill(this.b, 0, this.size, null);
        this.size = 0;
    }

    public L getLeft(final int index) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + this.size + ")");
        }
        return this.a[index];
    }

    public R getRight(final int index) {
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
        if (this.a != ObjectArrays.DEFAULT_EMPTY_ARRAY) {
            capacity = (int) Math.max(Math.min((long) this.a.length + (this.a.length >> 1), Arrays.MAX_ARRAY_SIZE), capacity);
        }
        else if (capacity < DEFAULT_INITIAL_CAPACITY) {
            capacity = DEFAULT_INITIAL_CAPACITY;
        }
        final Object[] t = new Object[capacity];
        System.arraycopy(this.a, 0, t, 0, this.size);
        this.a = (L[]) t;
        final Object[] u = new Object[capacity];
        System.arraycopy(this.b, 0, u, 0, this.size);
        this.b = (R[]) u;
        assert this.size <= this.a.length;
        assert this.size <= this.b.length;
    }

    public void set(final int index, final L l, final R r) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + this.size + ")");
        }
        this.a[index] = l;
        this.b[index] = r;
    }

    public L setLeft(final int index, final L l) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + this.size + ")");
        }
        L old = this.a[index];
        this.a[index] = l;
        return old;
    }

    public R setRight(final int index, final R r) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + this.size + ")");
        }
        R old = this.b[index];
        this.b[index] = r;
        return old;
    }

    public void shuffle(RandomGenerator rnd) {
        for (int i = this.size; i > 1; i--) {
            this.swap(i - 1, rnd.nextInt(i));
        }
    }

    public int size() {
        return this.size;
    }

    private void swap(int i, int j) {
        L tempL = this.getLeft(i);
        R tempR = this.getRight(i);
        this.set(i, this.getLeft(j), this.getRight(j));
        this.set(j, tempL, tempR);
    }
}
