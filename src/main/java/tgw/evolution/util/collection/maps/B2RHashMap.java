package tgw.evolution.util.collection.maps;

import it.unimi.dsi.fastutil.bytes.Byte2ReferenceOpenHashMap;
import tgw.evolution.Evolution;

public class B2RHashMap<V> extends Byte2ReferenceOpenHashMap<V> implements B2RMap<V> {

    @Override
    public FastEntrySet<V> byte2ReferenceEntrySet() {
        if (CHECKS) {
            Evolution.info("Allocating entry set!");
        }
        return super.byte2ReferenceEntrySet();
    }

    @Override
    public void trimCollection() {
        this.trim();
    }
}
