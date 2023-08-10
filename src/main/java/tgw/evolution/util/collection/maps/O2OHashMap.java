package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

import java.util.Map;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;

public class O2OHashMap<K, V> extends Object2ObjectOpenHashMap<K, V> implements O2OMap<K, V> {

    protected final O2OMap.Entry<K, V> entry = new O2OMap.Entry<>();
    /**
     * Bit 0: shouldRehash; <br>
     * Bit 1: askedToRehash; <br>
     */
    protected byte flags = 0b01;
    protected int lastPos = -1;

    public O2OHashMap() {
    }

    public O2OHashMap(int expected) {
        super(expected);
    }

    private void ensureCapacity(final int capacity) {
        final int needed = arraySize(capacity, this.f);
        if (needed > this.n) {
            this.rehash(needed);
        }
    }

    @Override
    public @Nullable O2OMap.Entry<K, V> fastEntries() {
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
                return this.entry.set(null, this.value[this.n]);
            }
        }
        for (int pos = this.lastPos; pos-- != 0; ) {
            K k = this.key[pos];
            if (k != null) {
                //Remember last pos
                this.lastPos = pos;
                return this.entry.set(k, this.value[pos]);
            }
        }
        this.lastPos = -1;
        this.entry.set(null, null);
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
    public FastEntrySet<K, V> object2ObjectEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set!");
        }
        return super.object2ObjectEntrySet();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m instanceof O2OMap) {
            if (this.f <= 0.5f) {
                this.ensureCapacity(m.size());
            }
            else {
                this.tryCapacity(this.size() + m.size());
            }
            O2OMap<? extends K, ? extends V> map = (O2OMap<? extends K, ? extends V>) m;
            for (O2OMap.Entry<? extends K, ? extends V> e = map.fastEntries(); e != null; e = map.fastEntries()) {
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
