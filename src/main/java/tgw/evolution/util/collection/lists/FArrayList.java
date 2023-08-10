package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
import tgw.evolution.Evolution;

public class FArrayList extends FloatArrayList implements FList {

    public FArrayList() {
        super();
    }

    public FArrayList(final FloatCollection c) {
        super(c);
    }

    @Override
    public Float get(int index) {
        Evolution.deprecatedMethod();
        return super.get(index);
    }

    @Override
    public FloatListIterator listIterator() {
        if (CHECKS) {
            Evolution.info("Allocating memory for an iterator");
        }
        return super.listIterator();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
