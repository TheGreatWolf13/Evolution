package tgw.evolution.patches;

public interface PatchTransientEntitySectionManager {

    default void startTicking_(long chunkPos) {
        throw new AbstractMethodError();
    }

    default void stopTicking_(long chunkPos) {
        throw new AbstractMethodError();
    }
}
