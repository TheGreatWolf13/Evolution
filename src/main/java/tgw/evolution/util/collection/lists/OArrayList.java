package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import tgw.evolution.Evolution;

import java.util.Arrays;
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

    public OArrayList(K[] a) {
        super(a);
    }

    public OArrayList() {
        super();
    }

    @Override
    public void addMany(K value, int length) {
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
    @Deprecated(forRemoval = true)
    public ObjectListIterator<K> listIterator() {
        if (CHECKS) {
            Evolution.warn("Allocating memory for an iterator");
        }
        return super.listIterator();
    }

    @Override
    public void setMany(K value, int start, int end) {
        if (start == end) {
            return;
        }
        Arrays.fill(this.a, start, end, value);
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
