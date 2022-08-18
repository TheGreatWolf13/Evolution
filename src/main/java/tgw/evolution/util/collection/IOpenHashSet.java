package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class IOpenHashSet extends IntOpenHashSet implements ISet {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
