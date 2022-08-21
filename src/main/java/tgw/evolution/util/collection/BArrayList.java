package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.bytes.ByteListIterator;
import tgw.evolution.Evolution;

import java.util.Collection;
import java.util.Iterator;

public class BArrayList extends ByteArrayList implements BList {

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
