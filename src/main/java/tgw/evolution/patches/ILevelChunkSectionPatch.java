package tgw.evolution.patches;

import tgw.evolution.capabilities.chunkstorage.AtmStorage;

public interface ILevelChunkSectionPatch {

    AtmStorage getAtmStorage();

    void setAtmStorage(AtmStorage atm);
}
