package tgw.evolution.patches;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.entity.Visibility;

public interface PatchPersistentEntitySectionManager {

    default boolean canPositionTick_BlockPos(int x, int z) {
        throw new AbstractMethodError();
    }

    default boolean canPositionTick_ChunkPos(long chunkPos) {
        throw new AbstractMethodError();
    }

    default void updateChunkStatus_(long chunkPos, Visibility visibility) {
        throw new AbstractMethodError();
    }

    default void updateChunkStatus_(long chunkPos, ChunkHolder.FullChunkStatus fullChunkStatus) {
        throw new AbstractMethodError();
    }
}
