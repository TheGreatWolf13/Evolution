package tgw.evolution.patches;

import tgw.evolution.util.collection.lists.OList;

public interface PatchClassInstanceMultiMap<T> {

    default OList<? extends T> find_(Class<T> clazz) {
        throw new AbstractMethodError();
    }
}
