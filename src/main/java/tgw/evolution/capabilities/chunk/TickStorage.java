package tgw.evolution.capabilities.chunk;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.longs.LongArrays;
import net.minecraft.nbt.CompoundTag;
import tgw.evolution.Evolution;

import java.util.Objects;

public class TickStorage {

    protected long[] a;
    protected int[] b;
    protected int size;

    public TickStorage() {
        this.a = LongArrays.DEFAULT_EMPTY_ARRAY; // We delay allocation
        this.b = IntArrays.DEFAULT_EMPTY_ARRAY; // We delay allocation
    }

    public boolean add(final long l, final int r) {
        this.grow(this.size + 1);
        this.a[this.size] = l;
        this.b[this.size++] = r;
        assert this.size <= this.a.length;
        assert this.size <= this.b.length;
        if (this.size == 1) {
            return false;
        }
        return this.a[this.size - 2] < l;
    }

    public void clear() {
        this.size = 0;
    }

    public void deserializeNbt(CompoundTag tag) {
        long[] ticks = tag.getLongArray("Ticks");
        int[] pos = tag.getIntArray("Pos");
        if (pos.length != ticks.length) {
            Evolution.error("Tick and pos length doesn't match! Ignoring...");
        }
        else {
            this.size = pos.length;
            this.grow(this.size);
            System.arraycopy(ticks, 0, this.a, 0, this.size);
            System.arraycopy(pos, 0, this.b, 0, this.size);
        }
    }

    public int getPos(int index) {
        return this.b[Objects.checkIndex(index, this.size)];
    }

    public long getTick(int index) {
        return this.a[Objects.checkIndex(index, this.size)];
    }

    private void grow(int capacity) {
        if (capacity <= this.a.length) {
            return;
        }
        //noinspection ArrayEquality
        if (this.a != LongArrays.DEFAULT_EMPTY_ARRAY) {
            capacity = (int) Math.max(Math.min((long) this.a.length + (this.a.length >> 1), Arrays.MAX_ARRAY_SIZE), capacity);
        }
        else if (capacity < 10) {
            capacity = 10;
        }
        this.a = LongArrays.forceCapacity(this.a, capacity, this.size);
        this.b = IntArrays.forceCapacity(this.b, capacity, this.size);
        assert this.size <= this.a.length;
        assert this.size <= this.b.length;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    /**
     * From is inclusive. To is exclusive.
     */
    public void removeElements(final int from, final int to) {
        Objects.checkFromToIndex(from, to, this.size);
        if (to != this.size) {
            System.arraycopy(this.a, to, this.a, from, this.size - to);
        }
        this.size -= to - from;
    }

    public CompoundTag serializeNbt() {
        CompoundTag tag = new CompoundTag();
        long[] ticks = new long[this.size];
        System.arraycopy(this.a, 0, ticks, 0, this.size);
        tag.putLongArray("Ticks", ticks);
        int[] pos = new int[this.size];
        System.arraycopy(this.b, 0, pos, 0, this.size);
        tag.putIntArray("Pos", pos);
        return tag;
    }

    public int size() {
        return this.size;
    }

    /**
     * Insertion sort to sort the ticks by their tick time. Earlier ticks are moved to the end of the list
     */
    public void sort() {
        long[] a = this.a;
        int[] b = this.b;
        for (int j = 1, len = this.size; j < len; ++j) {
            long key = a[j];
            int value = b[j];
            int i = j - 1;
            while (i >= 0 && a[i] < key) {
                a[i + 1] = a[i];
                b[i + 1] = b[i];
                --i;
            }
            a[i + 1] = key;
            b[i + 1] = value;
        }
    }
}
