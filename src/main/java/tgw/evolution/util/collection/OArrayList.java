package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import tgw.evolution.Evolution;

import java.util.Collection;

public class OArrayList<K> extends ObjectArrayList<K> implements OList<K> {

    public OArrayList(Collection<? extends K> c) {
        super(c);
    }

    public OArrayList(final int capacity) {
        super(capacity);
    }

    public OArrayList(ObjectCollection<? extends K> c) {
        super(c);
    }

    public OArrayList() {
        super();
    }

    @Override
    public ObjectIterator<K> it() {
        return super.listIterator();
    }

    @Override
    @Deprecated(forRemoval = true)
    public ObjectListIterator<K> iterator() {
        Evolution.info("Allocating memory for an iterator at {}", Thread.currentThread().getStackTrace()[3]);
        return super.iterator();
    }

    @Override
    @Deprecated(forRemoval = true)
    public ObjectListIterator<K> listIterator() {
        Evolution.info("Allocating memory for an iterator at {}", Thread.currentThread().getStackTrace()[3]);
        return super.listIterator();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
