package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.longs.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.Evolution;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class LArrayList extends LongArrayList implements LList {

    protected @Nullable View view;

    public LArrayList() {
        super();
    }

    public LArrayList(Collection<? extends Long> c) {
        super(c);
    }

    public LArrayList(LongCollection c) {
        super(c);
    }

    public LArrayList(LongList l) {
        super(l);
    }

    public LArrayList(long[] a) {
        super(a);
    }

    public LArrayList(long[] a, int offset, int length) {
        super(a, offset, length);
    }

    public LArrayList(int capacity) {
        super(capacity);
    }

    public LArrayList(Iterator<? extends Long> i) {
        super(i);
    }

    public LArrayList(LongIterator i) {
        super(i);
    }

    public LArrayList(long[] a, boolean wrapped) {
        super(a, wrapped);
    }

    @Override
    public void addMany(long value, int length) {
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
    public Long get(int index) {
        Evolution.deprecatedMethod();
        return super.get(index);
    }

    @Override
    public LongListIterator listIterator() {
        this.deprecatedListMethod();
        return super.listIterator();
    }

    @Override
    public void setMany(long value, int start, int end) {
        if (start == end) {
            return;
        }
        Arrays.fill(this.a, start, end, value);
    }

    @Override
    public void trimCollection() {
        this.trim();
    }

    @Override
    public @UnmodifiableView LList view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
