package tgw.evolution.mixin;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.*;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(DistanceManager.class)
public abstract class DistanceManagerMixin {

    @Shadow
    @Final
    Set<ChunkHolder> chunksToUpdateFutures;
    @Shadow
    @Final
    Executor mainThreadExecutor;
    @Shadow
    @Final
    Long2ObjectMap<ObjectSet<ServerPlayer>> playersPerChunk;
    @Shadow
    @Final
    ProcessorHandle<ChunkTaskPriorityQueueSorter.Release> ticketThrottlerReleaser;
    @Shadow
    @Final
    LongSet ticketsToRelease;
    @Shadow
    @Final
    private DistanceManager.FixedPlayerDistanceChunkTracker naturalSpawnChunkCounter;
    @Shadow
    @Final
    private DistanceManager.PlayerTicketTracker playerTicketManager;
    @Shadow
    @Final
    private DistanceManager.ChunkTicketTracker ticketTracker;
    @Shadow
    @Final
    private TickingTracker tickingTicketsTracker;

    /**
     * @author TheGreatWolf
     * @reason Avoid unnecessary allocations.
     */
    @Overwrite
    public void addPlayer(SectionPos sectionPos, ServerPlayer player) {
        ChunkPos chunkPos = sectionPos.chunk();
        long pos = chunkPos.toLong();
        ObjectSet<ServerPlayer> chunkPlayers = this.playersPerChunk.get(pos);
        if (chunkPlayers == null) {
            chunkPlayers = new ObjectOpenHashSet<>();
            this.playersPerChunk.put(pos, chunkPlayers);
        }
        chunkPlayers.add(player);
        this.naturalSpawnChunkCounter.update(pos, 0, true);
        this.playerTicketManager.update(pos, 0, true);
        this.tickingTicketsTracker.addTicket(TicketType.PLAYER, chunkPos, this.getPlayerTicketLevel(), chunkPos);
    }

    @Shadow
    public abstract int getPlayerTicketLevel();

    @Shadow
    protected abstract SortedArraySet<Ticket<?>> getTickets(long p_140858_);

    /**
     * @author TheGreatWolf
     * @reason Avoid most allocations
     */
    @Overwrite
    public boolean runAllUpdates(ChunkMap chunkMap) {
        this.naturalSpawnChunkCounter.runAllUpdates();
        this.tickingTicketsTracker.runAllUpdates();
        this.playerTicketManager.runAllUpdates();
        int i = Integer.MAX_VALUE - this.ticketTracker.runDistanceUpdates(Integer.MAX_VALUE);
        if (!this.chunksToUpdateFutures.isEmpty()) {
            for (ChunkHolder chunkHolder : this.chunksToUpdateFutures) {
                chunkHolder.updateFutures(chunkMap, this.mainThreadExecutor);
            }
            this.chunksToUpdateFutures.clear();
            return true;
        }
        if (!this.ticketsToRelease.isEmpty()) {
            LongIterator it = this.ticketsToRelease.iterator();
            while (it.hasNext()) {
                long pos = it.nextLong();
                boolean found = false;
                for (Ticket<?> ticket : this.getTickets(pos)) {
                    if (ticket.getType() == TicketType.PLAYER) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    ChunkHolder chunkHolder = chunkMap.getUpdatingChunkIfPresent(pos);
                    if (chunkHolder == null) {
                        throw new IllegalStateException();
                    }
                    CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>>
                            completablefuture
                            = chunkHolder.getEntityTickingChunkFuture();
                    //noinspection ObjectAllocationInLoop
                    completablefuture.thenAccept(either -> this.mainThreadExecutor.execute(
                            () -> this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
                            }, pos, false))));
                }
            }
            this.ticketsToRelease.clear();
        }
        return i != 0;
    }
}
