package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.bytes.ByteList;
import tgw.evolution.util.collection.ICollectionExtension;

public interface BList extends ByteList, ICollectionExtension {

    void addMany(byte value, int length);
}
