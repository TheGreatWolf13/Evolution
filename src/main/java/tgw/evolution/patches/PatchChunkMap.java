package tgw.evolution.patches;

import net.minecraft.server.level.ServerPlayer;
import tgw.evolution.util.collection.lists.OList;

public interface PatchChunkMap {

    default boolean anyPlayerCloseEnoughForSpawning(int chunkX, int chunkZ) {
        throw new AbstractMethodError();
    }

    default OList<ServerPlayer> getPlayersCloseForSpawning(int chunkX, int chunkZ) {
        throw new AbstractMethodError();
    }

    default void releaseLightTicket_(long chunkPos) {
        throw new AbstractMethodError();
    }
}
