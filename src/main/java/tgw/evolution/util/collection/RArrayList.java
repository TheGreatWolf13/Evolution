package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import tgw.evolution.Evolution;

import java.util.Collection;

public class RArrayList<K> extends ReferenceArrayList<K> implements RList<K> {

    public RArrayList(int capacity) {
        super(capacity);
    }

    public RArrayList() {
        super();
    }

    @SafeVarargs
    public RArrayList(K... a) {
        super(a);
    }

    public RArrayList(ReferenceCollection<? extends K> c) {
        super(c);
    }

    public RArrayList(Collection<? extends K> c) {
        super(c);
    }

    @Override
    public ObjectListIterator<K> iterator() {
        Evolution.info("Allocating memory for an iterator at {}", Thread.currentThread().getStackTrace()[3]);
        return super.iterator();
    }

    @Override
    public ObjectListIterator<K> listIterator() {
        Evolution.info("Allocating memory for an iterator at {}", Thread.currentThread().getStackTrace()[3]);
        return super.listIterator();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
