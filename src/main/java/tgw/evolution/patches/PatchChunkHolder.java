package tgw.evolution.patches;

public interface PatchChunkHolder {

    default void blockChanged_(int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
