package tgw.evolution.patches.replace.net.minecraft.client.searchtree;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.resources.ResourceLocation;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2IHashMap;
import tgw.evolution.util.collection.maps.O2IMap;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class ReloadableIdSearchTree<T> implements MutableSearchTree<T> {
    private final OList<T> contents = new OArrayList<>();
    private final Function<T, Stream<ResourceLocation>> idGetter;
    protected SuffixArray<T> namespaceTree = new SuffixArray<>();
    private final O2IMap<T> orderT = new O2IHashMap<>();
    protected SuffixArray<T> pathTree = new SuffixArray<>();

    public ReloadableIdSearchTree(Function<T, Stream<ResourceLocation>> idGetter) {
        this.idGetter = idGetter;
    }

    @Override
    public void add(T object) {
        this.orderT.put(object, this.contents.size());
        this.contents.add(object);
        this.index(object);
    }

    @Override
    public void clear() {
        this.contents.clear();
        this.orderT.clear();
    }

    protected int comparePosition(T a, T b) {
        return Integer.compare(this.orderT.getInt(a), this.orderT.getInt(b));
    }

    protected void index(T object) {
        this.idGetter.apply(object).forEach(resourceLocation -> {
            this.namespaceTree.add(object, resourceLocation.getNamespace().toLowerCase(Locale.ROOT));
            this.pathTree.add(object, resourceLocation.getPath().toLowerCase(Locale.ROOT));
        });
    }

    @Override
    public void refresh() {
        this.namespaceTree = new SuffixArray<>();
        this.pathTree = new SuffixArray<>();
        OList<T> contents = this.contents;
        for (int i = 0, len = contents.size(); i < len; ++i) {
            this.index(contents.get(i));
        }
        this.namespaceTree.generate();
        this.pathTree.generate();
    }

    @Override
    public OList<T> search(String string) {
        int colon = string.indexOf(':');
        if (colon == -1) {
            return this.pathTree.search(string);
        }
        OList<T> namespace = this.namespaceTree.search(string.substring(0, colon).trim());
        OList<T> path = this.pathTree.search(string.substring(colon + 1).trim());
        return new OArrayList<>(new IntersectionIterator<>(namespace.iterator(), path.iterator(), this::comparePosition));
    }

    @Environment(value = EnvType.CLIENT)
    protected static class IntersectionIterator<T> extends AbstractIterator<T> {
        private final PeekingIterator<T> firstIterator;
        private final Comparator<T> orderT;
        private final PeekingIterator<T> secondIterator;

        public IntersectionIterator(Iterator<T> iterator, Iterator<T> iterator2, Comparator<T> comparator) {
            this.firstIterator = Iterators.peekingIterator(iterator);
            this.secondIterator = Iterators.peekingIterator(iterator2);
            this.orderT = comparator;
        }

        @Override
        protected T computeNext() {
            while (this.firstIterator.hasNext() && this.secondIterator.hasNext()) {
                int i = this.orderT.compare(this.firstIterator.peek(), this.secondIterator.peek());
                if (i == 0) {
                    this.secondIterator.next();
                    return this.firstIterator.next();
                }
                if (i < 0) {
                    this.firstIterator.next();
                    continue;
                }
                this.secondIterator.next();
            }
            return this.endOfData();
        }
    }
}
