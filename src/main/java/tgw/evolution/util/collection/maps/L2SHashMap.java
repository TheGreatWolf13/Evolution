package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.longs.Long2ShortOpenHashMap;
import org.jetbrains.annotations.Nullable;

public class L2SHashMap extends Long2ShortOpenHashMap implements L2SMap {

    protected final L2SMap.Entry entry = new L2SMap.Entry();
    /**
     * Bit 0: shouldRehash; <br>
     * Bit 1: askedToRehash; <br>
     */
    protected byte flags = 0b01;
    protected int lastPos = -1;

    @Override
    public @Nullable L2SMap.Entry fastEntries() {
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
        this.entry.set(0L, (short) 0);
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