package tgw.evolution.patches;

public interface PatchChunkMap {

    boolean anyPlayerCloseEnoughForSpawning(int chunkX, int chunkZ);

    default void releaseLightTicket_(long chunkPos) {
        throw new AbstractMethodError();
    }
}
