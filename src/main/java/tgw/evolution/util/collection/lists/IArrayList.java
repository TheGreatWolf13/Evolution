package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.ints.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.Evolution;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class IArrayList extends IntArrayList implements IList {

    protected @Nullable View view;

    public IArrayList() {
        super();
    }

    public IArrayList(Collection<? extends Integer> c) {
        super(c);
    }

    public IArrayList(IntCollection c) {
        super(c);
    }

    public IArrayList(IntList l) {
        super(l);
    }

    public IArrayList(int[] a) {
        super(a);
    }

    public IArrayList(int[] a, int offset, int length) {
        super(a, offset, length);
    }

    public IArrayList(int capacity) {
        super(capacity);
    }

    public IArrayList(Iterator<? extends Integer> i) {
        super(i);
    }

    public IArrayList(IntIterator i) {
        super(i);
    }

    public IArrayList(int[] a, boolean wrapped) {
        super(a, wrapped);
    }

    @Override
    public void addMany(int value, int length) {
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
    public Integer get(int index) {
        Evolution.deprecatedMethod();
        return super.get(index);
    }

    @Override
    public IntListIterator listIterator() {
        this.deprecatedMethod();
        return super.listIterator();
    }

    @Override
    public void setMany(int value, int start, int end) {
        if (start == end) {
            return;
        }
        Arrays.fill(this.a, start, end, value);
    }

    @Override
    public @UnmodifiableView IList view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
