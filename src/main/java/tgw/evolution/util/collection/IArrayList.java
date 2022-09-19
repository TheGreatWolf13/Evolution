package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import tgw.evolution.Evolution;

import java.util.Collection;
import java.util.Iterator;

public class IArrayList extends IntArrayList implements IList {

    public IArrayList() {
        super();
    }

    public IArrayList(Iterator<? extends Integer> i) {
        super(i);
    }

    public IArrayList(Collection<? extends Integer> c) {
        super(c);
    }

    public IArrayList(IntIterator i) {
        super(i);
    }

    public IArrayList(IntCollection c) {
        super(c);
    }

    @Override
    public IntListIterator it() {
        return super.listIterator();
    }

    @Override
    public IntListIterator iterator() {
        Evolution.info("Allocating memory for an iterator at {}", Thread.currentThread().getStackTrace()[3]);
        return super.iterator();
    }

    @Override
    public IntListIterator listIterator() {
        Evolution.info("Allocating memory for an iterator at {}", Thread.currentThread().getStackTrace()[3]);
        return super.listIterator();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
