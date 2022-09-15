package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.bytes.Byte2ReferenceMap;

public interface B2RMap<V> extends Byte2ReferenceMap<V>, ICollectionExtension {

    @Override
    void clear();
}
