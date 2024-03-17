package tgw.evolution.mixin;

import net.minecraft.server.level.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchTicket;

@Mixin(DistanceManager.PlayerTicketTracker.class)
public abstract class MixinPlayerTicketTracker extends ChunkTracker {

    @Shadow(aliases = "this$0") @Final DistanceManager field_17463;

    public MixinPlayerTicketTracker(int i, int j, int k) {
        super(i, j, k);
    }

    @Shadow
    protected abstract boolean haveTicketFor(int i);

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void onLevelChange(long chunkPos, int i, boolean bl, boolean bl2) {
        if (bl != bl2) {
            Ticket<?> ticket = PatchTicket.newTicket(TicketType.PLAYER, DistanceManager.PLAYER_TICKET_LEVEL, chunkPos);
            if (bl2) {
                this.field_17463.ticketThrottlerInput.tell(ChunkTaskPriorityQueueSorter.message(() -> this.field_17463.mainThreadExecutor.execute(() -> {
                    if (this.haveTicketFor(this.getLevel(chunkPos))) {
                        this.field_17463.addTicket(chunkPos, ticket);
                        this.field_17463.ticketsToRelease.add(chunkPos);
                    }
                    else {
                        this.field_17463.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
                        }, chunkPos, false));
                    }
                }), chunkPos, () -> i));
            }
            else {
                this.field_17463.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> this.field_17463.mainThreadExecutor.execute(() -> this.field_17463.removeTicket(chunkPos, ticket)), chunkPos, true));
            }
        }
    }
}
