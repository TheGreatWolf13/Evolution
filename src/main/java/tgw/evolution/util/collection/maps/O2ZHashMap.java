package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.Evolution;

public class O2ZHashMap<K> extends Object2BooleanOpenHashMap<K> implements O2ZMap<K> {

    protected final O2ZMap.Entry<K> entry = new O2ZMap.Entry<>();
    /**
     * Bit 0: shouldRehash; <br>
     * Bit 1: askedToRehash; <br>
     */
    protected byte flags = 0b01;
    protected int lastPos = -1;
    protected @Nullable O2ZMap.View<K> view;

    @Override
    public @Nullable O2ZMap.Entry<K> fastEntries() {
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
        this.entry.set(null, false);
        this.handleRehash();
        return null;
    }

    @Override
    public Boolean get(Object key) {
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
    public @UnmodifiableView
    O2ZMap<K> view() {
        if (this.view == null) {
            this.view = new View<>(this);
        }
        return this.view;
    }
}
