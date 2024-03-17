package tgw.evolution.patches;

import net.minecraft.server.level.TicketType;

public interface PatchTickingTracker {

    default <T> void addTicket_(TicketType<T> ticketType, long chunkPos, int level, long key) {
        throw new AbstractMethodError();
    }

    default <T> void removeTicket_(TicketType<T> ticketType, long chunkPos, int level, long key) {
        throw new AbstractMethodError();
    }
}
