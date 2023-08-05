package tgw.evolution.util.collection;

import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

public class OArrayLimitedQueue<E> {

    protected final int limit;
    protected final OList<E> list = new OArrayList<>();

    public OArrayLimitedQueue(int limit) {
        this.limit = limit;
    }

    public void add(E e) {
        if (this.list.size() == this.limit) {
            this.list.remove(0);
        }
        this.list.add(e);
    }

    public OList<E> internalList() {
        return this.list;
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public int size() {
        return this.list.size();
    }
}
