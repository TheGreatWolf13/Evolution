package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.bytes.ByteListIterator;
import tgw.evolution.Evolution;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class BArrayList extends ByteArrayList implements BList {

    public BArrayList(int capacity) {
        super(capacity);
    }

    public BArrayList() {
        super();
    }

    public BArrayList(ByteIterator i) {
        super(i);
    }

    public BArrayList(ByteCollection c) {
        super(c);
    }

    public BArrayList(Iterator<? extends Byte> i) {
        super(i);
    }

    public BArrayList(Collection<? extends Byte> c) {
        super(c);
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
        if (value != 0) {
            Arrays.fill(this.a, size, size + length, value);
        }
        this.size = end;
    }

    @Override
    public ByteListIterator iterator() {
        Evolution.info("Allocating memory for an iterator at {}", Thread.currentThread().getStackTrace()[3]);
        return super.iterator();
    }

    @Override
    public ByteListIterator listIterator() {
        Evolution.info("Allocating memory for an iterator at {}", Thread.currentThread().getStackTrace()[3]);
        return super.listIterator();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
