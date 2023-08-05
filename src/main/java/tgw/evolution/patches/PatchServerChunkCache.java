package tgw.evolution.patches;

public interface PatchServerChunkCache {

    default void blockChanged_(int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
