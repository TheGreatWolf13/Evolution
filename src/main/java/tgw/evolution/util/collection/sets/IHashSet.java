package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;

public class IHashSet extends IntOpenHashSet implements ISet {

    protected final ISet.Entry fast = new ISet.Entry();
    /**
     * Bit 0: shouldRehash; <br>
     * Bit 1: askedToRehash; <br>
     */
    protected byte flags = 0b01;
    protected int lastPos = -1;

    @Override
    public boolean addAll(ISet set) {
        if (this.f <= 0.5f) {
            this.ensureCapacity(set.size());
        }
        else {
            this.tryCapacity(this.size() + set.size());
        }
        return ISet.super.addAll(set);
    }

    @Override
    public boolean addAll(IntCollection c) {
        if (c instanceof ISet set) {
            return this.addAll(set);
        }
        Evolution.warn("Default addAll");
        return super.addAll(c);
    }

    @Override
    public boolean containsAll(IntCollection c) {
        if (c instanceof ISet set) {
            return this.containsAll(set);
        }
        Evolution.warn("Default containsAll");
        return super.containsAll(c);
    }

    private void ensureCapacity(final int capacity) {
        final int needed = arraySize(capacity, this.f);
        if (needed > this.n) {
            this.rehash(needed);
        }
    }

    @Override
    public @Nullable ISet.Entry fastEntries() {
        if (this.isEmpty()) {
            this.handleRehash();
            return null;
        }
        if (this.lastPos == -1) {
            //Begin iteration
            this.lastPos = this.n;
            this.flags &= ~1;
            if (this.containsNull) {
                return this.fast.set(0);
            }
        }
        for (int pos = this.lastPos; pos-- != 0; ) {
            int k = this.key[pos];
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
    public IntIterator intIterator() {
        if (CHECKS) {
            Evolution.info("Allocating memory for an iterator!");
        }
        return super.intIterator();
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
    public boolean removeAll(IntCollection c) {
        if (c instanceof ISet set) {
            return this.removeAll(set);
        }
        Evolution.warn("Default removeAll");
        return super.removeAll(c);
    }

    @Override
    public void resetIteration() {
        this.flags = 0b01;
        this.lastPos = -1;
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
