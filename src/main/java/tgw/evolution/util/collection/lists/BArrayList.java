package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteListIterator;
import tgw.evolution.Evolution;

import java.util.Arrays;
import java.util.Iterator;

public class BArrayList extends ByteArrayList implements BList {

    public BArrayList() {
        super();
    }

    public BArrayList(Iterator<? extends Byte> i) {
        super(i);
    }

    @Override
    public void addMany(byte value, int length) {
        if (length < 0) {
            throw new NegativeArraySizeException("Length should be >= 0");
        }
        if (length == 0) {
            return;
        }
        int size = this.size();
        int end = size + length;
        this.ensureCapacity(size + length);
        Arrays.fill(this.a, size, size + length, value);
        this.size = end;
    }

    @Override
    public Byte get(int index) {
        Evolution.deprecatedMethod();
        return super.get(index);
    }

    @Override
    public ByteListIterator listIterator() {
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
