package tgw.evolution.patches;

import it.unimi.dsi.fastutil.longs.LongComparator;
import net.minecraft.server.level.TicketType;

public interface PatchTicketType {

    static <T> TicketType<T> create(String name, LongComparator comparator, long timeout) {
        //noinspection DataFlowIssue
        TicketType<T> ticketType = new TicketType<>(name, null, timeout);
        ticketType._setLongComparator(comparator);
        return ticketType;
    }

    /**
     * FOR INTERNAL USE ONLY. DO NOT CALL EXTERNALLY!
     */
    default void _setLongComparator(LongComparator comparator) {
        throw new AbstractMethodError();
    }

    default LongComparator longComparator() {
        throw new AbstractMethodError();
    }
}
