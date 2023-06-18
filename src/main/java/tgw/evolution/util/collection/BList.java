package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.bytes.ByteList;

public interface BList extends ByteList, ICollectionExtension {

    void addMany(byte value, int length);
}
