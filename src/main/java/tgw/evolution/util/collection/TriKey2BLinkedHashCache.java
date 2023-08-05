package tgw.evolution.util.collection;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;

public class TriKey2BLinkedHashCache<K1, K2, K3> extends Object2ByteLinkedOpenHashMap<TriKey2BLinkedHashCache.TriKey<K1, K2, K3>> {

    protected final int totalSize;

    public TriKey2BLinkedHashCache(int size, byte defRetValue) {
        super(size, 0.25f);
        this.totalSize = size;
        this.defaultReturnValue(defRetValue);
    }

    protected static <K1, K2, K3> int hashCode(K1 k1, K2 k2, K3 k3) {
        int result = k1.hashCode();
        result = 31 * result + k2.hashCode();
        result = 31 * result + k3.hashCode();
        return result;
    }

    public byte getAndMoveToFirst(K1 k1, K2 k2, K3 k3) {
        TriKey<K1, K2, K3> curr;
        final Object[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = (TriKey<K1, K2, K3>) key[pos = HashCommon.mix(hashCode(k1, k2, k3)) & this.mask]) == null) {
            return this.defRetValue;
        }
        if (curr.equals(k1, k2, k3)) {
            this.moveIndexToFirst(pos);
            return this.value[pos];
        }
        // There's always an unused entry.
        while (true) {
            if ((curr = (TriKey<K1, K2, K3>) key[pos = pos + 1 & this.mask]) == null) {
                return this.defRetValue;
            }
            if (curr.equals(k1, k2, k3)) {
                this.moveIndexToFirst(pos);
                return this.value[pos];
            }
        }
    }

    private void moveIndexToFirst(final int i) {
        if (this.size == 1 || this.first == i) {
            return;
        }
        if (this.last == i) {
            this.last = (int) (this.link[i] >>> 32);
            this.link[this.last] |= 0xFFFF_FFFFL;
        }
        else {
            final long linki = this.link[i];
            final int prev = (int) (linki >>> 32);
            final int next = (int) linki;
            this.link[prev] ^= (this.link[prev] ^ linki & 0xFFFF_FFFFL) & 0xFFFF_FFFFL;
            this.link[next] ^= (this.link[next] ^ linki & 0xFFFF_FFFF_0000_0000L) & 0xFFFF_FFFF_0000_0000L;
        }
        this.link[this.first] ^= (this.link[this.first] ^ (i & 0xFFFF_FFFFL) << 32) & 0xFFFF_FFFF_0000_0000L;
        this.link[i] = 0xFFFF_FFFFL << 32 | this.first & 0xFFFF_FFFFL;
        this.first = i;
    }

    @CanIgnoreReturnValue
    public byte putAndMoveToFirst(K1 k1, K2 k2, K3 k3, byte v) {
        return this.putAndMoveToFirst(new TriKey<>(k1, k2, k3), v);
    }

    @Override
    public byte putAndMoveToFirst(final TriKey<K1, K2, K3> k, final byte v) {
        if (this.size() == this.totalSize) {
            this.removeLastByte();
        }
        return super.putAndMoveToFirst(k, v);
    }

    @Override
    protected void rehash(int newN) {
        //Do nothing
    }

    protected static class TriKey<K1, K2, K3> {

        protected final K1 k1;
        protected final K2 k2;
        protected final K3 k3;

        public TriKey(K1 k1, K2 k2, K3 k3) {
            this.k1 = k1;
            this.k2 = k2;
            this.k3 = k3;
        }

        public boolean equals(K1 k1, K2 k2, K3 k3) {
            if (!this.k1.equals(k1)) {
                return false;
            }
            if (!this.k2.equals(k2)) {
                return false;
            }
            return this.k3.equals(k3);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            TriKey<?, ?, ?> triKey = (TriKey<?, ?, ?>) o;
            if (!this.k1.equals(triKey.k1)) {
                return false;
            }
            if (!this.k2.equals(triKey.k2)) {
                return false;
            }
            return this.k3.equals(triKey.k3);
        }

        @Override
        public int hashCode() {
            return TriKey2BLinkedHashCache.hashCode(this.k1, this.k2, this.k3);
        }
    }
}

