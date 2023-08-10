package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import tgw.evolution.Evolution;

import java.util.Arrays;
import java.util.Iterator;

public class IArrayList extends IntArrayList implements IList {

    public IArrayList() {
        super();
    }

    public IArrayList(int capacity) {
        super(capacity);
    }

    public IArrayList(Iterator<? extends Integer> i) {
        super(i);
    }

    @Override
    public void addMany(int value, int length) {
        if (length < 0) {
            throw new NegativeArraySizeException("Length should be >= 0");
        }
        if (length == 0) {
            return;
        }
        int size = this.size();
        int end = size + length;
        this.ensureCapacity(size + length);
        if (value != 0) {
            Arrays.fill(this.a, size, size + length, value);
        }
        this.size = end;
    }

    @Override
    public Integer get(int index) {
        Evolution.deprecatedMethod();
        return super.get(index);
    }

    @Override
    public IntListIterator listIterator() {
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
