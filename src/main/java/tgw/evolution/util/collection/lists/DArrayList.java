package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.doubles.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.Evolution;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class DArrayList extends DoubleArrayList implements DList {

    protected @Nullable View view;

    public DArrayList() {
        super();
    }

    public DArrayList(Collection<? extends Double> c) {
        super(c);
    }

    public DArrayList(DoubleCollection c) {
        super(c);
    }

    public DArrayList(DoubleList l) {
        super(l);
    }

    public DArrayList(double[] a) {
        super(a);
    }

    public DArrayList(double[] a, int offset, int length) {
        super(a, offset, length);
    }

    public DArrayList(double[] a, boolean wrapped) {
        super(a, wrapped);
    }

    public DArrayList(int capacity) {
        super(capacity);
    }

    public DArrayList(Iterator<? extends Double> i) {
        super(i);
    }

    public DArrayList(DoubleIterator i) {
        super(i);
    }

    @Override
    public void addMany(double value, int length) {
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
    public Double get(int index) {
        Evolution.deprecatedMethod();
        return super.get(index);
    }

    @Override
    public DoubleListIterator listIterator() {
        this.deprecatedMethod();
        return super.listIterator();
    }

    @Override
    public void setMany(double value, int start, int end) {
        if (start == end) {
            return;
        }
        Arrays.fill(this.a, start, end, value);
    }

    @Override
    public @UnmodifiableView DList view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
