package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;

public interface OList<K> extends ObjectList<K>, ICollectionExtension {

    ObjectIterator<K> it();
}
