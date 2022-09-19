package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;

public interface IList extends IntList, ICollectionExtension {

    IntListIterator it();
}
