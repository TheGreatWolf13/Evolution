package tgw.evolution.patches;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;

public interface PatchDistanceManager {

    default void addPlayer_(int secX, int secZ, ServerPlayer player) {
        throw new AbstractMethodError();
    }

    default <T> void addRegionTicket_(TicketType<T> ticketType, long chunkPos, int level, long key) {
        throw new AbstractMethodError();
    }

    default <T> void addTicket_(TicketType<T> ticketType, long chunkPos, int level, long key) {
        throw new AbstractMethodError();
    }

    default void removePlayer_(int secX, int secZ, ServerPlayer player) {
        throw new AbstractMethodError();
    }

    default <T> void removeRegionTicket_(TicketType<T> ticketType, long chunkPos, int level, long key) {
        throw new AbstractMethodError();
    }

    default <T> void removeTicket_(TicketType<T> ticketType, long chunkPos, int level, long key) {
        throw new AbstractMethodError();
    }

    default void updateChunkForced_(long chunkPos, boolean adding) {
        throw new AbstractMethodError();
    }
}
