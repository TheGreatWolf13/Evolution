package tgw.evolution.mixin;

import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchTicket;

@Mixin(Ticket.class)
public abstract class Mixin_CF_Ticket<T> implements Comparable<Ticket<?>>, PatchTicket {

    @Mutable @Shadow @Final @RestoreFinal public int ticketLevel;
    @Mutable @Shadow @Final @RestoreFinal public TicketType<T> type;
    @Shadow private long createdTick;
    @Shadow @Final @DeleteField private T key;
    @Unique private long longKey;

    @ModifyConstructor
    protected Mixin_CF_Ticket(TicketType<T> ticketType, int level, T key) {
        //noinspection ConstantValue,VariableNotUsedInsideIf
        if (key != null) {
            Evolution.deprecatedConstructor();
        }
        this.type = ticketType;
        this.ticketLevel = level;
    }

    @Override
    public void _setLongKey(long key) {
        this.longKey = key;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public int compareTo(Ticket<?> ticket) {
        int i = Integer.compare(this.ticketLevel, ticket.ticketLevel);
        if (i != 0) {
            return i;
        }
        int j = Integer.compare(System.identityHashCode(this.type), System.identityHashCode(ticket.type));
        return j != 0 ? j : this.type.longComparator().compare(this.longKey, ticket.longKey());
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Ticket)) {
            return false;
        }
        Ticket<?> ticket = (Ticket) object;
        return this.ticketLevel == ticket.ticketLevel && this.type.equals(ticket.type) && this.longKey == ticket.longKey();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    @Overwrite
    public int hashCode() {
        int hash = this.type.hashCode();
        hash = hash * 31 + this.ticketLevel;
        hash = hash * 31 + Long.hashCode(this.longKey);
        return hash;
    }

    @Override
    public long longKey() {
        return this.longKey;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public String toString() {
        return "Ticket[" + this.type + " " + this.ticketLevel + " (" + this.longKey + ")] at " + this.createdTick;
    }
}
