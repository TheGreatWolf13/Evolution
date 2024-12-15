package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.shorts.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class SArrayList extends ShortArrayList implements SList {

    protected @Nullable View view;

    public SArrayList() {
        super();
    }

    public SArrayList(Collection<? extends Short> c) {
        super(c);
    }

    public SArrayList(ShortCollection c) {
        super(c);
    }

    public SArrayList(ShortList l) {
        super(l);
    }

    public SArrayList(short[] a) {
        super(a);
    }

    public SArrayList(short[] a, int offset, int length) {
        super(a, offset, length);
    }

    public SArrayList(short[] a, boolean wrapped) {
        super(a, wrapped);
    }

    public SArrayList(int capacity) {
        super(capacity);
    }

    public SArrayList(Iterator<? extends Short> i) {
        super(i);
    }

    public SArrayList(ShortIterator i) {
        super(i);
    }

    @Override
    public void addMany(short value, int length) {
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
    public Short get(int index) {
        this.deprecatedMethod();
        return super.get(index);
    }

    @Override
    public ShortListIterator listIterator() {
        this.deprecatedMethod();
        return super.listIterator();
    }

    @Override
    public void setMany(short value, int start, int end) {
        if (start == end) {
            return;
        }
        Arrays.fill(this.a, start, end, value);
    }

    @Override
    public @UnmodifiableView SList view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
