package tgw.evolution.patches;

public interface PatchClientChunkCache {

    default void updateCameraViewCenter(int x, int z) {
        throw new AbstractMethodError();
    }
}
