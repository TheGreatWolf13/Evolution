package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

public class O2SHashMap<K> extends Object2ShortOpenHashMap<K> implements O2SMap<K> {

    protected final O2SMap.Entry<K> entry = new O2SMap.Entry<>();
    /**
     * Bit 0: shouldRehash; <br>
     * Bit 1: askedToRehash; <br>
     */
    protected byte flags = 0b01;
    protected int lastPos = -1;

    @Override
    public @Nullable O2SMap.Entry<K> fastEntries() {
        if (this.isEmpty()) {
            this.lastPos = -1;
            this.handleRehash();
            return null;
        }
        if (this.lastPos == -1) {
            //Begin iteration
            this.lastPos = this.n;
            this.flags &= ~1;
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
        this.entry.set(null, (short) 0);
        this.handleRehash();
        return null;
    }

    @Override
    public Short get(Object key) {
        Evolution.deprecatedMethod();
        return super.get(key);
    }

    protected void handleRehash() {
        byte oldFlags = this.flags;
        this.flags = 0b01;
        if ((oldFlags & 2) != 0) {
            this.trim();
        }
    }

    @Override
    public FastEntrySet<K> object2ShortEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set!");
        }
        return super.object2ShortEntrySet();
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
