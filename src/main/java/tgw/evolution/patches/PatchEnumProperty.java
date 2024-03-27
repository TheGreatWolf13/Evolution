package tgw.evolution.patches;

import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.collection.sets.RSet;

public interface PatchEnumProperty<K> {

    default O2OMap<String, K> names() {
        throw new AbstractMethodError();
    }

    default RSet<K> values() {
        throw new AbstractMethodError();
    }
}
