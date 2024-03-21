package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.util.collection.ICollectionExtension;

public interface RSet<K> extends ReferenceSet<K>, ICollectionExtension {

    static <K> @UnmodifiableView RSet<K> emptySet() {
        return EmptySet.EMPTY;
    }

    default boolean addAll(RSet<? extends K> set) {
        boolean modified = false;
        for (K e = set.fastEntries(); e != null; e = set.fastEntries()) {
            if (this.add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    default boolean containsAll(RSet<?> set) {
        for (Object e = set.fastEntries(); e != null; e = set.fastEntries()) {
            if (!this.contains(e)) {
                set.resetIteration();
                return false;
            }
        }
        return true;
    }

    @Nullable K fastEntries();

    /**
     * Gets the "first" element of this set. Used when collection size is 1.
     */
    @Nullable K getElement();

    default boolean removeAll(RSet<?> set) {
        boolean modified = false;
        for (K e = this.fastEntries(); e != null; e = this.fastEntries()) {
            if (set.contains(e)) {
                this.remove(e);
                modified = true;
            }
        }
        return modified;
    }

    void resetIteration();

    @UnmodifiableView RSet<K> view();

    class EmptySet<K> extends ReferenceSets.EmptySet<K> implements RSet<K> {

        protected static final EmptySet EMPTY = new EmptySet();

        protected EmptySet() {
        }

        @Override
        public boolean addAll(RSet<? extends K> set) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(RSet<?> set) {
            return false;
        }

        @Override
        public @Nullable K fastEntries() {
            return null;
        }

        @Override
        public @Nullable K getElement() {
            return null;
        }

        @Override
        public boolean removeAll(RSet<?> set) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void resetIteration() {
        }

        @Override
        public void trimCollection() {
        }

        @Override
        public @UnmodifiableView RSet<K> view() {
            return this;
        }
    }

    class View<K> extends ReferenceSets.UnmodifiableSet<K> implements RSet<K> {

        protected final RSet<K> s;

        protected View(RSet<K> s) {
            super(s);
            this.s = s;
        }

        @Override
        public @Nullable K fastEntries() {
            return this.s.fastEntries();
        }

        @Override
        public @Nullable K getElement() {
            return this.s.getElement();
        }

        @Override
        public void resetIteration() {
            this.s.resetIteration();
        }

        @Override
        public void trimCollection() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @UnmodifiableView RSet<K> view() {
            return this;
        }
    }
}
