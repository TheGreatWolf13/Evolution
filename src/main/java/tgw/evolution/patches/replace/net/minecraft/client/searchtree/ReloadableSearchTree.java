package tgw.evolution.patches.replace.net.minecraft.client.searchtree;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class ReloadableSearchTree<T> extends ReloadableIdSearchTree<T> {

    private final Function<T, Stream<String>> filler;
    protected SuffixArray<T> tree = new SuffixArray<>();

    public ReloadableSearchTree(Function<T, Stream<String>> filler, Function<T, Stream<ResourceLocation>> idGetter) {
        super(idGetter);
        this.filler = filler;
    }

    @Override
    protected void index(T object) {
        super.index(object);
        this.filler.apply(object).forEach(string -> this.tree.add(object, string.toLowerCase(Locale.ROOT)));
    }

    @Override
    public void refresh() {
        this.tree = new SuffixArray<>();
        super.refresh();
        this.tree.generate();
    }

    @Override
    public OList<T> search(String string) {
        int colon = string.indexOf(':');
        if (colon < 0) {
            return this.tree.search(string);
        }
        OList<T> namespace = this.namespaceTree.search(string.substring(0, colon).trim());
        String pathStr = string.substring(colon + 1).trim();
        OList<T> path = this.pathTree.search(pathStr);
        OList<T> otherPath = this.tree.search(pathStr);
        return new OArrayList<>(new IntersectionIterator<>(namespace.iterator(), new MergingUniqueIterator<>(path.iterator(), otherPath.iterator(), this::comparePosition), this::comparePosition));
    }

    @Environment(value = EnvType.CLIENT)
    static class MergingUniqueIterator<T> extends AbstractIterator<T> {
        private final PeekingIterator<T> firstIterator;
        private final Comparator<T> orderT;
        private final PeekingIterator<T> secondIterator;

        public MergingUniqueIterator(Iterator<T> iterator, Iterator<T> iterator2, Comparator<T> comparator) {
            this.firstIterator = Iterators.peekingIterator(iterator);
            this.secondIterator = Iterators.peekingIterator(iterator2);
            this.orderT = comparator;
        }

        @Override
        protected T computeNext() {
            boolean bl = !this.firstIterator.hasNext();
            boolean bl2 = !this.secondIterator.hasNext();
            if (bl && bl2) {
                return this.endOfData();
            }
            if (bl) {
                return this.secondIterator.next();
            }
            if (bl2) {
                return this.firstIterator.next();
            }
            int i = this.orderT.compare(this.firstIterator.peek(), this.secondIterator.peek());
            if (i == 0) {
                this.secondIterator.next();
            }
            return i <= 0 ? this.firstIterator.next() : this.secondIterator.next();
        }
    }
}
