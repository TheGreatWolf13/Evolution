package tgw.evolution.patches;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerPlayer;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.L2OMap;

public interface PatchChunkMap {

    default boolean anyPlayerCloseEnoughForSpawning(int chunkX, int chunkZ) {
        throw new AbstractMethodError();
    }

    default L2OMap<ChunkHolder> getChunks_() {
        throw new AbstractMethodError();
    }

    default OList<ServerPlayer> getPlayersCloseForSpawning(int chunkX, int chunkZ) {
        throw new AbstractMethodError();
    }

    default void releaseLightTicket_(long chunkPos) {
        throw new AbstractMethodError();
    }
}
