package tgw.evolution.patches;

import tgw.evolution.capabilities.chunk.AtmStorage;

public interface PatchLevelChunkSection {

    default AtmStorage getAtmStorage() {
        throw new AbstractMethodError();
    }

    default void setAtmStorage(AtmStorage atm) {
        throw new AbstractMethodError();
    }
}
