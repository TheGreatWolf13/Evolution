package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
import tgw.evolution.Evolution;

import java.util.Collection;

public class FArrayList extends FloatArrayList implements FList {

    public FArrayList() {
        super();
    }

    public FArrayList(final Collection<? extends Float> c) {
        super(c);
    }

    public FArrayList(final FloatCollection c) {
        super(c);
    }

    @Override
    public FloatListIterator iterator() {
        Evolution.info("Allocating memory for an iterator at {}", Thread.currentThread().getStackTrace()[3]);
        return super.iterator();
    }

    @Override
    public FloatListIterator listIterator() {
        Evolution.info("Allocating memory for an iterator at {}", Thread.currentThread().getStackTrace()[3]);
        return super.listIterator();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
