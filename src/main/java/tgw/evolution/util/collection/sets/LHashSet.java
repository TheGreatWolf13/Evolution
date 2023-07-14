package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import tgw.evolution.Evolution;

public class LHashSet extends LongOpenHashSet implements LSet {

    @Override
    public LongIterator longIterator() {
        if (CHECKS) {
            Evolution.info("Allocating memory for an iterator!");
        }
        return super.longIterator();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
