package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

public class O2LHashMap<K> extends Object2LongOpenHashMap<K> implements O2LMap<K> {

    protected final O2LMap.Entry<K> entry = new O2LMap.Entry<>();
    /**
     * Bit 0: shouldRehash; <br>
     * Bit 1: askedToRehash; <br>
     */
    protected byte flags = 0b01;
    protected int lastPos = -1;

    public O2LHashMap(int expected) {
        super(expected);
    }

    public O2LHashMap() {
    }

    @Override
    public @Nullable O2LMap.Entry<K> fastEntries() {
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
        this.entry.set(null, 0L);
        this.handleRehash();
        return null;
    }

    @Override
    public Long get(Object key) {
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
    public FastEntrySet<K> object2LongEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set!");
        }
        return super.object2LongEntrySet();
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
