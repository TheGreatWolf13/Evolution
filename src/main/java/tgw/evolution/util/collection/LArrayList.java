package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import tgw.evolution.Evolution;

import java.util.Iterator;

public class LArrayList extends LongArrayList implements LList {

    public LArrayList() {
        super();
    }

    public LArrayList(LongIterator i) {
        super(i);
    }

    public LArrayList(Iterator<? extends Long> i) {
        super(i);
    }

    @Override
    public LongListIterator iterator() {
        Evolution.info("Allocating memory for an iterator at {}", Thread.currentThread().getStackTrace()[3]);
        return super.iterator();
    }

    @Override
    public LongListIterator listIterator() {
        Evolution.info("Allocating memory for an iterator at {}", Thread.currentThread().getStackTrace()[3]);
        return super.listIterator();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
