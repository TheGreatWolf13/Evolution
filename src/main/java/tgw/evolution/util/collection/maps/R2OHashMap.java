package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

import java.util.Map;

public class R2OHashMap<K, V> extends Reference2ObjectOpenHashMap<K, V> implements R2OMap<K, V> {

    protected final R2OMap.Entry<K, V> entry = new R2OMap.Entry<>();
    /**
     * Bit 0: shouldRehash; <br>
     * Bit 1: askedToRehash; <br>
     */
    protected byte flags = 0b01;
    protected int lastPos = -1;
    private View<K, V> view;

    public R2OHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public R2OHashMap() {
    }

    @Override
    public R2OMap.@Nullable Entry<K, V> fastEntries() {
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
    public FastEntrySet<K, V> reference2ObjectEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set");
        }
        return super.reference2ObjectEntrySet();
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

    @Override
    public R2OMap<K, V> view() {
        if (this.view == null) {
            this.view = new View<>(this);
        }
        return this.view;
    }
}
