package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import tgw.evolution.Evolution;

import java.util.Iterator;

public class LArrayList extends LongArrayList implements LList {

    public LArrayList() {
        super();
    }

    public LArrayList(Iterator<? extends Long> i) {
        super(i);
    }

    @Override
    public LongListIterator listIterator() {
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
