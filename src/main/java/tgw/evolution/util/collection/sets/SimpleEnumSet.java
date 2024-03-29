package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.ArrayHelper;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.NoSuchElementException;

public class SimpleEnumSet<E extends Enum<E>> extends AbstractSet<E> implements RSet<E> {

    protected final Class<E> clazz;
    protected long data;
    protected final E[] values;
    protected @Nullable View<E> view;

    public SimpleEnumSet(Class<E> clazz) {
        if (!clazz.isEnum()) {
            throw new IllegalStateException("Class is not an Enum!");
        }
        E[] enums = ArrayHelper.getOrCacheArray(clazz);
        if (enums.length > 64) {
            throw new IllegalStateException("Maximum supported size is 64!");
        }
        this.values = enums;
        this.clazz = clazz;
    }

    @Contract("_, _ -> new")
    public static <E extends Enum<E>> SimpleEnumSet<E> of(Class<E> clazz, Collection<E> collection) {
        SimpleEnumSet<E> set = new SimpleEnumSet<>(clazz);
        set.addAll(collection);
        return set;
    }

    @Contract("_ -> new")
    public static <E extends Enum<E>> SimpleEnumSet<E> of(E[] e) {
        SimpleEnumSet<E> set = new SimpleEnumSet<>((Class<E>) e.getClass().getComponentType());
        for (E e1 : e) {
            set.add(e1);
        }
        return set;
    }

    @Contract("_ -> new")
    public static <E extends Enum<E>> SimpleEnumSet<E> of(E e) {
        SimpleEnumSet<E> set = new SimpleEnumSet<>((Class<E>) e.getClass());
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
            return RSet.super.addAll(c);
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
    public long beginIteration() {
        int size = this.size();
        if (size == 0) {
            return 0;
        }
        for (int i = 0, len = this.values.length; i < len; i++) {
            if ((this.data & 1L << i) != 0) {
                return (long) i << 32 | size;
            }
        }
        throw new IllegalStateException("Should never reach here");
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
        if (oClass != this.clazz && oClass.getSuperclass() != this.clazz) {
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
    public E getIteration(long it) {
        int pos = (int) (it >> 32);
        return this.values[pos];
    }

    @Override
    public E getSampleElement() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Empty set");
        }
        for (int i = 0, len = this.values.length; i < len; i++) {
            if ((this.data & 1L << i) != 0) {
                return this.values[i];
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public int hashCode() {
        int h = this.size();
        for (long it = this.beginIteration(); this.hasNextIteration(it); it = this.nextEntry(it)) {
            E e = this.getIteration(it);
            h = h * 31 + e.hashCode();
        }
        return h;
    }

    @Contract(pure = true)
    @Override
    public boolean isEmpty() {
        return this.data == 0;
    }

    @Override
    public ObjectIterator<E> iterator() {
        this.deprecatedMethod();
        return new EnumIterator();
    }

    @Override
    public long nextEntry(long it) {
        if (this.isEmpty()) {
            return 0;
        }
        int size = (int) it;
        if (--size == 0) {
            return 0;
        }
        int pos = (int) (it >> 32) + 1;
        for (int i = pos, len = this.values.length; i < len; i++) {
            if ((this.data & 1L << i) != 0) {
                return (long) i << 32 | size;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }

    @Override
    public boolean remove(Object o) {
        Class<?> oClass = o.getClass();
        if (oClass != this.clazz && oClass.getSuperclass() != this.clazz) {
            return false;
        }
        long oldData = this.data;
        this.data &= ~(1L << this.clazz.cast(o).ordinal());
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
    public long removeIteration(long it) {
        int pos = (int) (it >> 32);
        this.remove(this.values[pos]);
        return it;
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
    public <T> T[] toArray(T[] a) {
        int size = this.size();
        T[] r = a.length >= size ? a : (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        int i = 0;
        for (long it = this.beginIteration(); this.hasNextIteration(it); it = this.nextEntry(it)) {
            r[i++] = (T) this.getIteration(it);
        }
        return r;
    }

    @Override
    public boolean trim() {
        return false;
    }

    @Override
    public @UnmodifiableView RSet<E> view() {
        if (this.view == null) {
            this.view = new View<>(this);
        }
        return this.view;
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
