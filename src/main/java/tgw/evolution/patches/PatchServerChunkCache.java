package tgw.evolution.patches;

import net.minecraft.server.level.TicketType;

public interface PatchServerChunkCache {

    default <T> void addRegionTicket_(TicketType<T> ticketType, long chunkPos, int level, long key) {
        throw new AbstractMethodError();
    }

    default void blockChanged_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default <T> void removeRegionTicket_(TicketType<T> ticketType, long chunkPos, int level, long key) {
        throw new AbstractMethodError();
    }

    default void updateChunkForced_(long chunkPos, boolean adding) {
        throw new AbstractMethodError();
    }
}
