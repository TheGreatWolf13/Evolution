package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.bytes.Byte2ReferenceMap;
import tgw.evolution.util.collection.ICollectionExtension;

public interface B2RMap<V> extends Byte2ReferenceMap<V>, ICollectionExtension {

    @Override
    void clear();
}
