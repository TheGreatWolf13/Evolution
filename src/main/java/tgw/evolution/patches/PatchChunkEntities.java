package tgw.evolution.patches;

import tgw.evolution.util.collection.lists.OList;

public interface PatchChunkEntities<T> {

    default long getChunkPos_() {
        throw new AbstractMethodError();
    }

    default OList<T> getEntities_() {
        throw new AbstractMethodError();
    }
}
