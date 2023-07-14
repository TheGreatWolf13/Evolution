package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import tgw.evolution.Evolution;

public class IHashSet extends IntOpenHashSet implements ISet {

    @Override
    public IntIterator intIterator() {
        if (CHECKS) {
            Evolution.info("Allocating memory for an iterator!");
        }
        return super.intIterator();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
