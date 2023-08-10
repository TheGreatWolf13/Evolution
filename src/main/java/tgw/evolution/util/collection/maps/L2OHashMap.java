package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

import java.util.Map;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;

public class L2OHashMap<V> extends Long2ObjectOpenHashMap<V> implements L2OMap<V> {

    protected final L2OMap.Entry<V> entry = new L2OMap.Entry<>();
    /**
     * Bit 0: shouldRehash; <br>
     * Bit 1: askedToRehash; <br>
     */
    protected byte flags = 0b01;
    protected int lastPos = -1;

    private void ensureCapacity(final int capacity) {
        final int needed = arraySize(capacity, this.f);
        if (needed > this.n) {
            this.rehash(needed);
        }
    }

    @Override
    public L2OMap.@Nullable Entry<V> fastEntries() {
        if (this.isEmpty()) {
            this.lastPos = -1;
            this.handleRehash();
            return null;
        }
        if (this.lastPos == -1) {
            //Begin iteration
            this.lastPos = this.n;
            this.flags &= ~1;
            if (this.containsNullKey) {
                return this.entry.set(0L, this.value[this.n]);
            }
        }
        for (int pos = this.lastPos; pos-- != 0; ) {
            long k = this.key[pos];
            if (k != 0) {
                //Remember last pos
                this.lastPos = pos;
                return this.entry.set(k, this.value[pos]);
            }
        }
        this.lastPos = -1;
        this.entry.set(0L, null);
        this.handleRehash();
        return null;
    }

    protected void handleRehash() {
        byte oldFlags = this.flags;
        this.flags = 0b01;
        if ((oldFlags & 2) != 0) {
            this.trim();
        }
    }

    @Override
    public FastEntrySet<V> long2ObjectEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set!");
        }
        return super.long2ObjectEntrySet();
    }

    @Override
    public void putAll(Map<? extends Long, ? extends V> m) {
        if (m instanceof L2OMap) {
            if (this.f <= 0.5f) {
                this.ensureCapacity(m.size());
            }
            else {
                this.tryCapacity(this.size() + m.size());
            }
            L2OMap<? extends V> map = (L2OMap<? extends V>) m;
            for (L2OMap.Entry<? extends V> e = map.fastEntries(); e != null; e = map.fastEntries()) {
                this.put(e.k, e.v);
            }
        }
        else {
            super.putAll(m);
        }
    }

    @Override
    protected void rehash(int newN) {
        if ((this.flags & 1) != 0) {
            super.rehash(newN);
        }
        else {
            this.flags |= 2;
        }
    }

    @Override
    public void trimCollection() {
        this.trim();
    }

    private void tryCapacity(final long capacity) {
        final int needed = (int) Math.min(1 << 30, Math.max(2, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity / this.f))));
        if (needed > this.n) {
            this.rehash(needed);
        }
    }
}
