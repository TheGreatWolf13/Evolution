package tgw.evolution.patches;

import tgw.evolution.capabilities.chunk.AtmStorage;
import tgw.evolution.capabilities.chunk.IntegrityStorage;
import tgw.evolution.capabilities.chunk.StabilityStorage;

public interface PatchLevelChunkSection {

    default AtmStorage getAtmStorage() {
        throw new AbstractMethodError();
    }

    default IntegrityStorage getIntegrityStorage() {
        throw new AbstractMethodError();
    }

    default IntegrityStorage getLoadFactorStorage() {
        throw new AbstractMethodError();
    }

    default StabilityStorage getStabilityStorage() {
        throw new AbstractMethodError();
    }

    default void setAtmStorage(AtmStorage atm) {
        throw new AbstractMethodError();
    }

    default void setIntegrityStorage(IntegrityStorage storage) {
        throw new AbstractMethodError();
    }

    default void setLoadFactorStorage(IntegrityStorage storage) {
        throw new AbstractMethodError();
    }

    default void setStabilityStorage(StabilityStorage storage) {
        throw new AbstractMethodError();
    }
}
