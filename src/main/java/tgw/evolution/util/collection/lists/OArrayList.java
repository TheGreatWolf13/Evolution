package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.Collection;

public class OArrayList<K> extends ObjectArrayList<K> implements OList<K> {

    protected @Nullable OList<K> view;

    public OArrayList(Collection<? extends K> c) {
        super(c.size());
        this.addAll(c);
    }

    public OArrayList(Iterable<? extends K> i) {
        super();
        this.addAll(i);
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
    public boolean addAll(OList<? extends K> list) {
        int n = list.size();
        if (n == 0) {
            return false;
        }
        this.grow(this.size + n);
        list.getElements(0, this.a, this.size, n);
        this.size += n;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        return OList.super.addAll(c);
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
        this.grow(size + length);
        Arrays.fill(this.a, size, size + length, value);
        this.size = end;
    }

    private void grow(int capacity) {
        if (capacity > this.a.length) {
            //noinspection ArrayEquality
            if (this.a != ObjectArrays.DEFAULT_EMPTY_ARRAY) {
                capacity = (int) Math.max(Math.min(this.a.length + (long) (this.a.length >> 1), Integer.MAX_VALUE - 8), capacity);
            }
            else if (capacity < 10) {
                capacity = 10;
            }
            if (this.wrapped) {
                this.a = ObjectArrays.forceCapacity(this.a, capacity, this.size);
            }
            else {
                Object[] t = new Object[capacity];
                System.arraycopy(this.a, 0, t, 0, this.size);
                this.a = (K[]) t;
            }
            assert this.size <= this.a.length;
        }
    }

    @Override
    public ObjectListIterator<K> listIterator() {
        this.deprecatedMethod();
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
    public String toString() {
        StringBuilder s = new StringBuilder();
        int i = 0;
        int n = this.size();
        boolean first = true;
        s.append("[");
        while (n-- != 0) {
            if (first) {
                first = false;
            }
            else {
                s.append(", ");
            }
            K k = this.get(i++);
            if (this == k) {
                s.append("(this list)");
            }
            else {
                s.append(k);
            }
        }
        s.append("]");
        return s.toString();
    }

    @Override
    public @UnmodifiableView OList<K> view() {
        if (this.view == null) {
            this.view = new View<>(this);
        }
        return this.view;
    }
}
