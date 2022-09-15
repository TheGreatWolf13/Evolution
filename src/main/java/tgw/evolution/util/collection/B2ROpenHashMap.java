package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.bytes.Byte2ReferenceOpenHashMap;

public class B2ROpenHashMap<V> extends Byte2ReferenceOpenHashMap<V> implements B2RMap<V> {

    @Override
    public void trimCollection() {
        this.trim();
    }
}
