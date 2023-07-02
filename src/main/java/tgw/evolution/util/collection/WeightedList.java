package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import org.jetbrains.annotations.Contract;

import java.util.random.RandomGenerator;

public class WeightedList<K> implements ICollectionExtension {

    /**
     * The backing array of elements.
     */
    protected K[] a;
    /**
     * The current actual size of the list (never greater than the backing-array
     * length).
     */
    protected int size;
    /**
     * The sum of all the weights (always greater than or equal to size).
     */
    protected int totalWeight;
    /**
     * The backing array of weights.
     */
    protected int[] w;

    public WeightedList() {
        this.a = (K[]) ObjectArrays.EMPTY_ARRAY;
        this.w = IntArrays.EMPTY_ARRAY;
    }

    @Contract(mutates = "this")
    public void add(final K k, final int weight) {
        if (weight < 1) {
            throw new IllegalStateException("Weight must be at least 1");
        }
        this.grow(this.size + 1);
        this.a[this.size] = k;
        this.w[this.size++] = weight;
        this.totalWeight += weight;
        assert this.totalWeight >= this.size;
        assert this.size <= this.a.length;
        assert this.size <= this.w.length;
    }

    @Contract(mutates = "this")
    @Override
    public void clear() {
        java.util.Arrays.fill(this.a, 0, this.size, null);
        this.size = 0;
        this.totalWeight = 0;
    }

    @Contract(pure = true)
    public K get(final int index) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + this.size + ")");
        }
        return this.a[index];
    }

    @Contract(pure = true)
    public K getWeighted(RandomGenerator random) {
        if (this.isEmpty()) {
            throw new NullPointerException("List is empty");
        }
        int chosen = random.nextInt(this.totalWeight);
        for (int i = 0; i < this.size; ++i) {
            chosen -= this.w[i];
            if (chosen < 0) {
                return this.a[i];
            }
        }
        throw new IllegalStateException("Should not reach here");
    }

    /**
     * Grows this array list, ensuring that it can contain the given number of
     * entries without resizing, and in case increasing the current capacity at
     * least by a factor of 50%.
     *
     * @param capacity the new minimum capacity for this array list.
     */
    @Contract(pure = true)
    private void grow(int capacity) {
        if (capacity <= this.a.length) {
            return;
        }
        //noinspection ArrayEquality
        if (this.a != ObjectArrays.DEFAULT_EMPTY_ARRAY) {
            capacity = (int) Math.max(Math.min((long) this.a.length + (this.a.length >> 1), Arrays.MAX_ARRAY_SIZE), capacity);
        }
        else if (capacity < 10) {
            capacity = 10;
        }
        final Object[] t = new Object[capacity];
        System.arraycopy(this.a, 0, t, 0, this.size);
        this.a = (K[]) t;
        final int[] u = new int[capacity];
        System.arraycopy(this.w, 0, u, 0, this.size);
        this.w = u;
        assert this.size <= this.a.length;
        assert this.size <= this.w.length;
    }

    @Contract(pure = true)
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Contract(pure = true)
    public int size() {
        return this.size;
    }

    @Contract(pure = true)
    @Override
    public void trimCollection() {
        if (0 == this.a.length || this.size == this.a.length) {
            return;
        }
        final K[] t = (K[]) new Object[this.size];
        System.arraycopy(this.a, 0, t, 0, this.size);
        this.a = t;
        final int[] u = new int[this.size];
        System.arraycopy(this.w, 0, u, 0, this.size);
        this.w = u;
        assert this.size <= this.a.length;
    }
}
