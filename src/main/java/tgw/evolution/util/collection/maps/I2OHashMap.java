package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

public class I2OHashMap<V> extends Int2ObjectOpenHashMap<V> implements I2OMap<V> {

    protected final I2OMap.Entry<V> entry = new I2OMap.Entry<>();
    /**
     * Bit 0: shouldRehash; <br>
     * Bit 1: askedToRehash; <br>
     */
    protected byte flags = 0b01;
    protected int lastPos = -1;

    @Override
    public I2OMap.@Nullable Entry<V> fastEntries() {
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
                return this.entry.set(0, this.value[this.n]);
            }
        }
        for (int pos = this.lastPos; pos-- != 0; ) {
            int k = this.key[pos];
            if (k != 0) {
                //Remember last pos
                this.lastPos = pos;
                return this.entry.set(k, this.value[pos]);
            }
        }
        this.lastPos = -1;
        this.entry.set(0, null);
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
    public FastEntrySet<V> int2ObjectEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set!");
        }
        return super.int2ObjectEntrySet();
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
}
