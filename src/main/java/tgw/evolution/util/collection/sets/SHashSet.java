package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

public class SHashSet extends ShortOpenHashSet implements SSet {

    protected final Entry element = new Entry();
    protected final Entry fast = new Entry();
    /**
     * Bit 0: shouldRehash; <br>
     * Bit 1: askedToRehash; <br>
     */
    protected byte flags = 0b01;
    protected int lastPos = -1;

    @Override
    public @Nullable Entry fastEntries() {
        if (this.isEmpty()) {
            this.handleRehash();
            return null;
        }
        if (this.lastPos == -1) {
            //Begin iteration
            this.lastPos = this.n;
            this.flags &= ~1;
            if (this.containsNull) {
                return this.fast.set((short) 0);
            }
        }
        for (int pos = this.lastPos; pos-- != 0; ) {
            short k = this.key[pos];
            if (k != 0) {
                //Remember last pos
                this.lastPos = pos;
                return this.fast.set(k);
            }
        }
        this.lastPos = -1;
        this.handleRehash();
        return null;
    }

    @Override
    public @Nullable Entry getElement() {
        if (this.isEmpty()) {
            return null;
        }
        if (this.containsNull) {
            return this.element.set((short) 0);
        }
        for (int pos = this.n; pos-- != 0; ) {
            short k = this.key[pos];
            if (k != 0) {
                return this.element.set(k);
            }
        }
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
    public ShortIterator iterator() {
        if (CHECKS) {
            Evolution.info("Allocating memory for an iterator!");
        }
        return super.iterator();
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
