package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.floats.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class FArrayList extends FloatArrayList implements FList {

    protected @Nullable View view;

    public FArrayList() {
        super();
    }

    public FArrayList(Collection<? extends Float> c) {
        super(c);
    }

    public FArrayList(final FloatCollection c) {
        super(c);
    }

    public FArrayList(FloatList l) {
        super(l);
    }

    public FArrayList(float[] a) {
        super(a);
    }

    public FArrayList(float[] a, int offset, int length) {
        super(a, offset, length);
    }

    public FArrayList(Iterator<? extends Float> i) {
        super(i);
    }

    public FArrayList(FloatIterator i) {
        super(i);
    }

    public FArrayList(float[] a, boolean wrapped) {
        super(a, wrapped);
    }

    public FArrayList(int capacity) {
        super(capacity);
    }

    @Override
    public void addMany(float value, int length) {
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
    public Float get(int index) {
        this.deprecatedMethod();
        return super.get(index);
    }

    @Override
    public FloatListIterator listIterator() {
        this.deprecatedMethod();
        return super.listIterator();
    }

    @Override
    public void setMany(float value, int start, int end) {
        if (start == end) {
            return;
        }
        Arrays.fill(this.a, start, end, value);
    }

    @Override
    public @UnmodifiableView FList view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
