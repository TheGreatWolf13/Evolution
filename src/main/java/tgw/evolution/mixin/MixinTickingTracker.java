package tgw.evolution.mixin;

import net.minecraft.server.level.ChunkTracker;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.TickingTracker;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchTicket;
import tgw.evolution.patches.PatchTickingTracker;

@Mixin(TickingTracker.class)
public abstract class MixinTickingTracker extends ChunkTracker implements PatchTickingTracker {

    public MixinTickingTracker(int i, int j, int k) {
        super(i, j, k);
    }

    @Shadow
    public abstract void addTicket(long l, Ticket<?> ticket);

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public <T> void addTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object) {
        Evolution.deprecatedMethod();
        throw new RuntimeException("Use non-ChunkPos version!");
    }

    @Override
    public <T> void addTicket_(TicketType<T> ticketType, long chunkPos, int level, long key) {
        this.addTicket(chunkPos, PatchTicket.newTicket(ticketType, level, key));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public <T> void removeTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object) {
        Evolution.deprecatedMethod();
        throw new RuntimeException("Use non-ChunkPos version!");
    }

    @Shadow
    public abstract void removeTicket(long l, Ticket<?> ticket);

    @Override
    public <T> void removeTicket_(TicketType<T> ticketType, long chunkPos, int level, long key) {
        this.removeTicket(chunkPos, PatchTicket.newTicket(ticketType, level, key));
    }
}
