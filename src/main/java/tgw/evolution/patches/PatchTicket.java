package tgw.evolution.patches;

import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;

public interface PatchTicket {

    static <T> Ticket<T> newTicket(TicketType<T> ticketType, int level, long key) {
        //noinspection DataFlowIssue
        Ticket<T> ticket = new Ticket<>(ticketType, level, null);
        ticket._setLongKey(key);
        return ticket;
    }

    /**
     * FOR INTERNAL USE ONLY. DO NOT CALL EXTERNALLY.
     */
    default void _setLongKey(long key) {
        throw new AbstractMethodError();
    }

    default long longKey() {
        throw new AbstractMethodError();
    }
}
