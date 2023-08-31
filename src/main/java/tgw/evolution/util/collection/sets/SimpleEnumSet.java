package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.NoSuchElementException;

public class SimpleEnumSet<E extends Enum<E>> extends AbstractSet<E> implements RSet<E> {

    protected final Class<E> clazz;
    protected final E[] values;
    protected long data;
    protected int lastPos = -1;

    public SimpleEnumSet(Class<E> clazz, E[] values) {
        if (!clazz.isEnum()) {
            throw new IllegalStateException("Class is not an Enum!");
        }
        if (values.length > 64) {
            throw new IllegalStateException("Maximum supported size is 64!");
        }
        this.values = values;
        this.clazz = clazz;
    }

    @Contract("_, _ -> new")
    public static <E extends Enum<E>> SimpleEnumSet<E> of(E[] values, E e) {
        SimpleEnumSet<E> set = new SimpleEnumSet<>((Class<E>) e.getClass(), values);
        set.add(e);
        return set;
    }

    @Override
    public boolean add(E e) {
        long oldData = this.data;
        this.data |= 1L << e.ordinal();
        return oldData != this.data;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (!(c instanceof SimpleEnumSet<?> es)) {
            return super.addAll(c);
        }
        if (es.clazz != this.clazz) {
            if (es.isEmpty()) {
                return false;
            }
            throw new ClassCastException(es.clazz + " != " + this.clazz);
        }
        long oldData = this.data;
        this.data |= es.data;
        return this.data != oldData;
    }

    @Override
    public void clear() {
        this.data = 0;
    }

    @Contract(pure = true)
    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        Class<?> oClass = o.getClass();
        if (oClass != this.clazz) {
            return false;
        }
        return (this.data & 1L << this.clazz.cast(o).ordinal()) != 0;
    }

    @Contract(pure = true)
    @Override
    public boolean containsAll(Collection<?> c) {
        if (!(c instanceof SimpleEnumSet<?> es)) {
            return super.containsAll(c);
        }
        if (es.clazz != this.clazz) {
            return es.isEmpty();
        }
        return (es.data & ~this.data) == 0;
    }

    @Override
    public @Nullable E fastEntries() {
        if (this.isEmpty()) {
            return null;
        }
        if (this.lastPos == -1) {
            //Begin iteration
            this.lastPos = this.values.length;
        }
        for (int pos = this.lastPos; pos-- != 0; ) {
            if ((this.data & 1L << pos) != 0) {
                //Remember last pos
                this.lastPos = pos;
                return this.values[pos];
            }
        }
        this.lastPos = -1;
        return null;
    }

    @Contract(pure = true)
    @Override
    public boolean isEmpty() {
        return this.data == 0;
    }

    @Override
    public ObjectIterator<E> iterator() {
        if (CHECKS) {
            Evolution.info("Allocating memory for an iterator!");
        }
        return new EnumIterator();
    }

    @Override
    public boolean remove(Object o) {
        Class<?> oClass = o.getClass();
        if (oClass != this.clazz) {
            return false;
        }
        long oldData = this.data;
        this.data &= ~(1L << this.clazz.cast(o).ordinal());
        return this.data != oldData;
    }

    @Override
    public boolean removeAll(RSet<?> set) {
        if (!(set instanceof SimpleEnumSet<?> es)) {
            return RSet.super.removeAll(set);
        }
        if (es.clazz != this.clazz) {
            return false;
        }
        long oldData = this.data;
        this.data &= ~es.data;
        return this.data != oldData;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (!(c instanceof SimpleEnumSet<?> es)) {
            return super.removeAll(c);
        }
        if (es.clazz != this.clazz) {
            return false;
        }
        long oldData = this.data;
        this.data &= ~es.data;
        return this.data != oldData;
    }

    @Override
    public void resetIteration() {
        this.lastPos = -1;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (!(c instanceof SimpleEnumSet<?> es)) {
            return super.retainAll(c);
        }
        if (es.clazz != this.clazz) {
            boolean changed = this.data != 0;
            this.data = 0;
            return changed;
        }
        long oldData = this.data;
        this.data &= es.data;
        return this.data != oldData;
    }

    @Override
    @Contract(pure = true)
    public int size() {
        return Long.bitCount(this.data);
    }

    @Override
    public void trimCollection() {
        //Nothing to do
    }

    private class EnumIterator implements ObjectIterator<E> {
        /**
         * The bit representing the last element returned by this iterator
         * but not removed, or zero if no such element exists.
         */
        long lastReturned;
        /**
         * A bit vector representing the elements in the set not yet
         * returned by this iterator.
         */
        long unseen;

        EnumIterator() {
            this.unseen = SimpleEnumSet.this.data;
        }

        @Contract(pure = true)
        @Override
        public boolean hasNext() {
            return this.unseen != 0;
        }

        @Override
        public E next() {
            if (this.unseen == 0) {
                throw new NoSuchElementException();
            }
            this.lastReturned = this.unseen & -this.unseen;
            this.unseen -= this.lastReturned;
            return SimpleEnumSet.this.values[Long.numberOfTrailingZeros(this.lastReturned)];
        }

        @Override
        public void remove() {
            if (this.lastReturned == 0) {
                throw new IllegalStateException();
            }
            SimpleEnumSet.this.data &= ~this.lastReturned;
            this.lastReturned = 0;
        }
    }
}
