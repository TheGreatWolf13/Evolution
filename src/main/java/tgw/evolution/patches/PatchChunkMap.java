package tgw.evolution.patches;

public interface PatchChunkMap {

    default void releaseLightTicket_(long chunkPos) {
        throw new AbstractMethodError();
    }
}
