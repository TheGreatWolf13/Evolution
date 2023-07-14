package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.booleans.BooleanArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EitherList<L, R> {

    protected Object[] a;
    protected boolean[] b;
    protected int size;

    public EitherList() {
        this.a = ObjectArrays.EMPTY_ARRAY;
        this.b = BooleanArrays.EMPTY_ARRAY;
    }

    @Contract(mutates = "this")
    public void addLeft(L l) {
        this.grow(this.size + 1);
        this.a[this.size] = l;
        this.b[this.size] = false;
        ++this.size;
    }

    @Contract(mutates = "this")
    public void addRight(int index, R r) {
        Objects.checkIndex(index, this.size + 1);
        this.grow(this.size + 1);
        if (index != this.size) {
            System.arraycopy(this.a, index, this.a, index + 1, this.size - index);
            System.arraycopy(this.b, index, this.b, index + 1, this.size - index);
        }
        this.a[index] = r;
        this.b[index] = true;
        ++this.size;
    }

    @Contract(mutates = "this")
    public void addRight(R r) {
        this.grow(this.size + 1);
        this.a[this.size] = r;
        this.b[this.size] = true;
        ++this.size;
    }

    @Contract(mutates = "this")
    public void clear() {
        java.util.Arrays.fill(this.a, 0, this.size, null);
        this.size = 0;
    }

    @Contract(pure = true)
    public L getLeft(int index) {
        Objects.checkIndex(index, this.size);
        return (L) this.a[index];
    }

    @Contract(pure = true)
    public @Nullable L getLeftOrNull(int index) {
        Objects.checkIndex(index, this.size);
        if (!this.b[index]) {
            return (L) this.a[index];
        }
        return null;
    }

    @Contract(pure = true)
    public R getRight(int index) {
        Objects.checkIndex(index, this.size);
        return (R) this.a[index];
    }

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
        this.a = t;
        final boolean[] u = new boolean[capacity];
        System.arraycopy(this.b, 0, u, 0, this.size);
        this.b = u;
        assert this.size <= this.a.length;
        assert this.size <= this.b.length;
    }

    @Contract(pure = true)
    public boolean isLeft(int index) {
        Objects.checkIndex(index, this.size);
        return !this.b[index];
    }

    @Contract(pure = true)
    public int size() {
        return this.size;
    }
}
