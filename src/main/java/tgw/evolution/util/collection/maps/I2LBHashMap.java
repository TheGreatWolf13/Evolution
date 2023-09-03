package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.HashCommon;
import tgw.evolution.util.collection.sets.ISet;

import java.util.Arrays;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;
import static it.unimi.dsi.fastutil.HashCommon.maxFill;

public class I2LBHashMap {

    protected final float f;
    protected final int minN;
    protected boolean containsNullKey;
    protected int[] key;
    protected int mask;
    protected int maxFill;
    protected int n;
    protected int size;
    protected long[] value1;
    protected byte[] value2;

    public I2LBHashMap() {
        this(16, 0.75f);
    }

    public I2LBHashMap(final int expected, final float f) {
        if (f <= 0 || f >= 1) {
            throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than 1");
        }
        if (expected < 0) {
            throw new IllegalArgumentException("The expected number of elements must be non-negative");
        }
        this.f = f;
        this.minN = this.n = arraySize(expected, f);
        this.mask = this.n - 1;
        this.maxFill = maxFill(this.n, f);
        this.key = new int[this.n + 1];
        this.value1 = new long[this.n + 1];
        this.value2 = new byte[this.n + 1];
    }

    public void clear() {
        if (this.size == 0) {
            return;
        }
        this.size = 0;
        this.containsNullKey = false;
        Arrays.fill(this.key, 0);
    }

    public void getAll(ISet all) {
        if (this.size == 0) {
            return;
        }
        if (this.containsNullKey) {
            all.add(0);
        }
        for (int pos = this.n; pos-- != 0; ) {
            int k = this.key[pos];
            if (k != 0) {
                all.add(k);
            }
        }
    }

    public byte getByteByIndex(int index) {
        if (index >= 0) {
            return this.value2[index];
        }
        return 0;
    }

    public int getIndexFor(int k) {
        if (k == 0) {
            return this.containsNullKey ? this.n : -(this.n + 1);
        }
        int curr;
        final int[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = HashCommon.mix(k) & this.mask]) == 0) {
            return -(pos + 1);
        }
        if (k == curr) {
            return pos;
        }
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = pos + 1 & this.mask]) == 0) {
                return -(pos + 1);
            }
            if (k == curr) {
                return pos;
            }
        }
    }

    public long getLongByIndex(int index) {
        if (index >= 0) {
            return this.value1[index];
        }
        return 0L;
    }

    private void insert(final int pos, final int k, final long v1, final byte v2) {
        if (pos == this.n) {
            this.containsNullKey = true;
        }
        this.key[pos] = k;
        this.value1[pos] = v1;
        this.value2[pos] = v2;
        if (this.size++ >= this.maxFill) {
            this.rehash(arraySize(this.size + 1, this.f));
        }
    }

    public void put(final int k, final long v1, final byte v2) {
        final int pos = this.getIndexFor(k);
        if (pos < 0) {
            this.insert(-pos - 1, k, v1, v2);
            return;
        }
        this.value1[pos] = v1;
        this.value2[pos] = v2;
    }

    private int realSize() {
        return this.containsNullKey ? this.size - 1 : this.size;
    }

    protected void rehash(final int newN) {
        final int[] key = this.key;
        final long[] value1 = this.value1;
        final byte[] value2 = this.value2;
        final int mask = newN - 1; // Note that this is used by the hashing macro
        final int[] newKey = new int[newN + 1];
        final long[] newValue1 = new long[newN + 1];
        final byte[] newValue2 = new byte[newN + 1];
        int i = this.n;
        for (int j = this.realSize(); j-- != 0; ) {
            while (key[--i] == 0) {
                //
            }
            int pos;
            if (!(newKey[pos = HashCommon.mix(key[i]) & mask] == 0)) {
                while (!(newKey[pos = pos + 1 & mask] == 0)) {
                    //
                }
            }
            newKey[pos] = key[i];
            newValue1[pos] = value1[i];
            newValue2[pos] = value2[i];
        }
        newValue1[newN] = value1[this.n];
        newValue2[newN] = value2[this.n];
        this.n = newN;
        this.mask = mask;
        this.maxFill = maxFill(this.n, this.f);
        this.key = newKey;
        this.value1 = newValue1;
        this.value2 = newValue2;
    }

    public void remove(int k) {
        if (k == 0) {
            if (this.containsNullKey) {
                this.removeNullEntry();
            }
            return;
        }
        int curr;
        final int[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = HashCommon.mix(k) & this.mask]) == 0) {
            return;
        }
        if (k == curr) {
            this.removeEntry(pos);
            return;
        }
        while (true) {
            if ((curr = key[pos = pos + 1 & this.mask]) == 0) {
                return;
            }
            if (k == curr) {
                this.removeEntry(pos);
                return;
            }
        }
    }

    private void removeEntry(final int pos) {
        this.size--;
        this.shiftKeys(pos);
        if (this.n > this.minN && this.size < this.maxFill / 4 && this.n > 16) {
            this.rehash(this.n / 2);
        }
    }

    private void removeNullEntry() {
        this.containsNullKey = false;
        this.size--;
        if (this.n > this.minN && this.size < this.maxFill / 4 && this.n > 16) {
            this.rehash(this.n / 2);
        }
    }

    protected final void shiftKeys(int pos) {
        // Shift entries with the same hash.
        final int[] key = this.key;
        for (; ; ) {
            int last;
            pos = (last = pos) + 1 & this.mask;
            int curr;
            for (; ; ) {
                if ((curr = key[pos]) == 0) {
                    key[last] = 0;
                    return;
                }
                int slot = HashCommon.mix(curr) & this.mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
                    break;
                }
                pos = pos + 1 & this.mask;
            }
            key[last] = curr;
            this.value1[last] = this.value1[pos];
            this.value2[last] = this.value2[pos];
        }
    }
}
