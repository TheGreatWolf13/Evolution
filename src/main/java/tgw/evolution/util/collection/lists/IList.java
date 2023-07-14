package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.ints.IntList;
import tgw.evolution.util.collection.ICollectionExtension;

public interface IList extends IntList, ICollectionExtension {

    void addMany(int value, int length);
}
