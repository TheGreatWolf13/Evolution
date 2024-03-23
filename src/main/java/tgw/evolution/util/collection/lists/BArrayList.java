package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.bytes.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.Evolution;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class BArrayList extends ByteArrayList implements BList {

    protected @Nullable View view;

    public BArrayList() {
        super();
    }

    public BArrayList(Collection<? extends Byte> c) {
        super(c);
    }

    public BArrayList(ByteCollection c) {
        super(c);
    }

    public BArrayList(ByteList l) {
        super(l);
    }

    public BArrayList(byte[] a) {
        super(a);
    }

    public BArrayList(byte[] a, int offset, int length) {
        super(a, offset, length);
    }

    public BArrayList(byte[] a, boolean wrapped) {
        super(a, wrapped);
    }

    public BArrayList(int capacity) {
        super(capacity);
    }

    public BArrayList(Iterator<? extends Byte> i) {
        super(i);
    }

    public BArrayList(ByteIterator i) {
        super(i);
    }

    @Override
    public void addMany(byte value, int length) {
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
    public Byte get(int index) {
        Evolution.deprecatedMethod();
        return super.get(index);
    }

    @Override
    public ByteListIterator listIterator() {
        this.deprecatedMethod();
        return super.listIterator();
    }

    @Override
    public void setMany(byte value, int start, int end) {
        if (start == end) {
            return;
        }
        Arrays.fill(this.a, start, end, value);
    }

    @Override
    public @UnmodifiableView BList view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
