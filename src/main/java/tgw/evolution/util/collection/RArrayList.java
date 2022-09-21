package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import tgw.evolution.Evolution;

import java.util.Collection;

@SuppressWarnings("EqualsAndHashcode")
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
    public int hashCode() {
        int hash = 1;
        for (int i = 0, l = this.size(); i < l; i++) {
            hash = 31 * hash + System.identityHashCode(this.get(i));
        }
        return hash;
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
