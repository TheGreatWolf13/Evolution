package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

public class LHashSet extends LongOpenHashSet implements LSet {

    protected final LSet.Entry fast = new LSet.Entry();
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
                return this.fast.set(0L);
            }
        }
        for (int pos = this.lastPos; pos-- != 0; ) {
            long k = this.key[pos];
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

    protected void handleRehash() {
        byte oldFlags = this.flags;
        this.flags = 0b01;
        if ((oldFlags & 2) != 0) {
            this.trim();
        }
    }

    @Override
    public LongIterator longIterator() {
        if (CHECKS) {
            Evolution.info("Allocating memory for an iterator!");
        }
        return super.longIterator();
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
