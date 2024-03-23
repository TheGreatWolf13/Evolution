package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.Collection;

public class OArrayList<K> extends ObjectArrayList<K> implements OList<K> {

    protected @Nullable OList<K> view;

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
    public @UnmodifiableView OList<K> view() {
        if (this.view == null) {
            this.view = new View<>(this);
        }
        return this.view;
    }
}
