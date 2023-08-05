package tgw.evolution.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.*;
import net.minecraft.util.Mth;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.network.PacketSCUpdateCameraViewCenter;
import tgw.evolution.patches.PatchEither;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2ZMap;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.collection.sets.LSet;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

@Mixin(ChunkMap.class)
public abstract class MixinChunkMap extends ChunkStorage {

    @Shadow @Final private static Logger LOGGER;
    @Unique private final LSet chunksLoaded = new LHashSet();
    @Unique private final LSet chunksToLoad = new LHashSet();
    @Unique private final LSet chunksToUnload = new LHashSet();
    @Shadow public int viewDistance;
    @Shadow @Final ServerLevel level;
    @Shadow @Final private ChunkMap.DistanceManager distanceManager;
    @Shadow @Final private Int2ObjectMap<ChunkMap.TrackedEntity> entityMap;
    @Shadow @Final private ThreadedLevelLightEngine lightEngine;
    @Shadow @Final private BlockableEventLoop<Runnable> mainThreadExecutor;
    @Shadow @Final private ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailbox;
    @Shadow @Final private PlayerMap playerMap;
    @Shadow @Final private PoiManager poiManager;
    @Shadow @Final private AtomicInteger tickingGenerated;
    @Shadow @Final private Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap;
    @Shadow private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap;

    public MixinChunkMap(Path pRegionFolder, DataFixer pFixerUpper, boolean pSync) {
        super(pRegionFolder, pFixerUpper, pSync);
    }

    @Contract(value = "_, _, _, _, _ -> _")
    @Shadow
    public static boolean isChunkInRange(int p_200879_, int p_200880_, int p_200881_, int p_200882_, int p_200883_) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Contract(value = "_, _, _, _, _ -> _")
    @Shadow
    private static boolean isChunkOnRangeBorder(int p_183829_, int p_183830_, int p_183831_, int p_183832_, int p_183833_) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Overwrite
    public boolean anyPlayerCloseEnoughForSpawning(ChunkPos chunkPos) {
        if (!this.distanceManager.hasPlayersNearby(chunkPos.toLong())) {
            return false;
        }
        O2ZMap<ServerPlayer> playerMap = this.playerMap.getPlayerMap();
        for (O2ZMap.Entry<ServerPlayer> e = playerMap.fastEntries(); e != null; e = playerMap.fastEntries()) {
            if (this.playerIsCloseEnoughForSpawning(e.key(), chunkPos)) {
                return true;
            }
        }
        return false;
    }

    @Shadow
    protected abstract CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> getChunkRangeFuture(ChunkPos chunkPos,
                                                                                                                         int i,
                                                                                                                         IntFunction<ChunkStatus> intFunction);

    @Overwrite
    public List<ServerPlayer> getPlayers(ChunkPos pos, boolean boundaryOnly) {
        OList<ServerPlayer> list = null;
        //Cannot use fastEntries as apparently this runs asynchronously
        for (ServerPlayer player : this.playerMap.getPlayers(0L)) {
            SectionPos lastSectionPos = player.getLastSectionPos();
            if (boundaryOnly && isChunkOnRangeBorder(pos.x, pos.z, lastSectionPos.x(), lastSectionPos.z(), this.viewDistance) ||
                !boundaryOnly && isChunkInRange(pos.x, pos.z, lastSectionPos.x(), lastSectionPos.z(), this.viewDistance)) {
                if (list == null) {
                    list = new OArrayList<>();
                }
                list.add(player);
                continue;
            }
            SectionPos lastCameraSectionPos = player.getLastCameraSectionPos();
            if (lastCameraSectionPos != null) {
                if (boundaryOnly && isChunkOnRangeBorder(pos.x, pos.z, lastCameraSectionPos.x(), lastCameraSectionPos.z(), this.viewDistance) ||
                    !boundaryOnly && isChunkInRange(pos.x, pos.z, lastCameraSectionPos.x(), lastCameraSectionPos.z(), this.viewDistance)) {
                    if (list == null) {
                        list = new OArrayList<>();
                    }
                    list.add(player);
                }
            }
        }
        return list == null ? OList.emptyList() : list.view();
    }

    @Shadow
    protected abstract @Nullable ChunkHolder getVisibleChunkIfPresent(long p_140328_);

    @Shadow
    protected abstract boolean isExistingChunkFull(ChunkPos pChunkPos);

    @Shadow
    protected abstract byte markPosition(ChunkPos p_140230_, ChunkStatus.ChunkType p_140231_);

    /**
     * @author TheGreatWolf
     * @reason Avoid a few allocations.
     */
    @Overwrite
    public void move(ServerPlayer player) {
        for (ChunkMap.TrackedEntity trackedEntity : this.entityMap.values()) {
            if (trackedEntity.entity == player) {
                trackedEntity.updatePlayers(this.level.players());
            }
            else {
                trackedEntity.updatePlayer(player);
            }
        }
        SectionPos lastSectionPos = player.getLastSectionPos();
        SectionPos currentSectionPos = SectionPos.of(player);
        long lastChunk = ChunkPos.asLong(lastSectionPos.x(), lastSectionPos.z());
        long currentChunk = ChunkPos.asLong(currentSectionPos.x(), currentSectionPos.z());
        boolean ignored = this.playerMap.ignored(player);
        boolean skip = this.skipPlayer(player);
        boolean diffSections = lastSectionPos.asLong() != currentSectionPos.asLong();
        boolean shouldRemovePlayerTicket = false;
        boolean shouldAddPlayerTicket = false;
        if (diffSections || ignored != skip) {
            this.updatePlayerPos(player);
            BlockPos pos = player.getCamera().blockPosition();
            player.connection.send(
                    new PacketSCUpdateCameraViewCenter(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())));
            if (!ignored) {
                shouldRemovePlayerTicket = true;
            }
            if (!skip) {
                shouldAddPlayerTicket = true;
            }
            if (!ignored && skip) {
                this.playerMap.ignorePlayer(player);
            }
            if (ignored && !skip) {
                this.playerMap.unIgnorePlayer(player);
            }
            if (lastChunk != currentChunk) {
                this.playerMap.updatePlayer(lastChunk, currentChunk, player);
            }
        }
        LSet chunksToLoad = this.chunksToLoad;
        chunksToLoad.clear();
        LSet chunksToUnload = this.chunksToUnload;
        chunksToUnload.clear();
        this.chunksLoaded.clear();
        int sectionX = currentSectionPos.x();
        int sectionZ = currentSectionPos.z();
        int lastX = lastSectionPos.x();
        int lastZ = lastSectionPos.z();
        if (Math.abs(lastX - sectionX) <= this.viewDistance * 2 && Math.abs(lastZ - sectionZ) <= this.viewDistance * 2) {
            int minX = Math.min(sectionX, lastX) - this.viewDistance - 1;
            int minZ = Math.min(sectionZ, lastZ) - this.viewDistance - 1;
            int maxX = Math.max(sectionX, lastX) + this.viewDistance + 1;
            int maxZ = Math.max(sectionZ, lastZ) + this.viewDistance + 1;
            for (int dx = minX; dx <= maxX; ++dx) {
                for (int dz = minZ; dz <= maxZ; ++dz) {
                    boolean wasLoaded = isChunkInRange(dx, dz, lastX, lastZ, this.viewDistance);
                    boolean load = isChunkInRange(dx, dz, sectionX, sectionZ, this.viewDistance);
                    if (load != wasLoaded) {
                        if (load) {
                            chunksToLoad.add(ChunkPos.asLong(dx, dz));
                        }
                        else {
                            chunksToUnload.add(ChunkPos.asLong(dx, dz));
                        }
                    }
                    else if (load) {
                        this.chunksLoaded.add(ChunkPos.asLong(dx, dz));
                    }
                }
            }
        }
        else {
            //Unloads chunks
            for (int dx = lastX - this.viewDistance - 1; dx <= lastX + this.viewDistance + 1; ++dx) {
                for (int dz = lastZ - this.viewDistance - 1; dz <= lastZ + this.viewDistance + 1; ++dz) {
                    if (isChunkInRange(dx, dz, lastX, lastZ, this.viewDistance)) {
                        chunksToUnload.add(ChunkPos.asLong(dx, dz));
                    }
                }
            }
            //Loads chunks
            for (int dx = sectionX - this.viewDistance - 1; dx <= sectionX + this.viewDistance + 1; ++dx) {
                for (int dz = sectionZ - this.viewDistance - 1; dz <= sectionZ + this.viewDistance + 1; ++dz) {
                    if (isChunkInRange(dx, dz, sectionX, sectionZ, this.viewDistance)) {
                        chunksToLoad.add(ChunkPos.asLong(dx, dz));
                    }
                }
            }
        }
        //Handle camera chunks
        boolean shouldRemoveCameraTicket = false;
        boolean shouldAddCameraTicket = false;
        boolean shouldUnloadAll = false;
        boolean shouldCameraLoad = true;
        SectionPos lastCameraSectionPos = player.getLastCameraSectionPos();
        SectionPos currentCameraSectionPos = null;
        if (player.getCamera() == player) {
            if (!player.getCameraUnload()) {
                shouldCameraLoad = false;
            }
            else {
                shouldUnloadAll = true;
                shouldRemoveCameraTicket = true;
            }
        }
        if (shouldCameraLoad) {
            player.setCameraUnload(!shouldUnloadAll);
            boolean firstTick = false;
            if (lastCameraSectionPos == null) {
                lastCameraSectionPos = lastSectionPos;
                firstTick = true;
            }
            currentCameraSectionPos = SectionPos.of(player.getCamera());
            sectionX = currentCameraSectionPos.getX();
            sectionZ = currentCameraSectionPos.getZ();
            if (firstTick || lastCameraSectionPos.asLong() != currentCameraSectionPos.asLong()) {
                shouldAddCameraTicket = true;
                shouldRemoveCameraTicket = !firstTick;
                player.connection.send(new PacketSCUpdateCameraViewCenter(sectionX, sectionZ));
            }
            player.setLastCameraSectionPos(shouldUnloadAll ? null : currentCameraSectionPos);
            lastX = lastCameraSectionPos.x();
            lastZ = lastCameraSectionPos.z();
            if (!shouldUnloadAll && Math.abs(lastX - sectionX) <= this.viewDistance * 2 && Math.abs(lastZ - sectionZ) <= this.viewDistance * 2) {
                int minX = Math.min(sectionX, lastX) - this.viewDistance - 1;
                int minZ = Math.min(sectionZ, lastZ) - this.viewDistance - 1;
                int maxX = Math.max(sectionX, lastX) + this.viewDistance + 1;
                int maxZ = Math.max(sectionZ, lastZ) + this.viewDistance + 1;
                for (int dx = minX; dx <= maxX; ++dx) {
                    for (int dz = minZ; dz <= maxZ; ++dz) {
                        boolean wasLoaded = isChunkInRange(dx, dz, lastX, lastZ, this.viewDistance);
                        boolean load = isChunkInRange(dx, dz, sectionX, sectionZ, this.viewDistance);
                        long pos = ChunkPos.asLong(dx, dz);
                        if (load) {
                            if (!this.chunksLoaded.contains(pos)) {
                                if (wasLoaded) {
                                    if (chunksToUnload.remove(pos)) {
                                        chunksToLoad.add(pos);
                                    }
                                }
                                else if (chunksToLoad.add(pos)) {
                                    chunksToUnload.remove(pos);
                                }
                            }
                        }
                        else if (wasLoaded) {
                            if (!this.chunksLoaded.contains(pos) && !chunksToLoad.contains(pos)) {
                                chunksToUnload.add(pos);
                            }
                        }
                    }
                }
            }
            else {
                //Unloads chunks
                for (int dx = lastX - this.viewDistance - 1; dx <= lastX + this.viewDistance + 1; ++dx) {
                    for (int dz = lastZ - this.viewDistance - 1; dz <= lastZ + this.viewDistance + 1; ++dz) {
                        if (isChunkInRange(dx, dz, lastX, lastZ, this.viewDistance)) {
                            long pos = ChunkPos.asLong(dx, dz);
                            if (!this.chunksLoaded.contains(pos) && !chunksToLoad.contains(pos)) {
                                chunksToUnload.add(pos);
                            }
                        }
                    }
                }
                //Loads chunks
                if (!shouldUnloadAll) {
                    for (int dx = sectionX - this.viewDistance - 1; dx <= sectionX + this.viewDistance + 1; ++dx) {
                        for (int dz = sectionZ - this.viewDistance - 1; dz <= sectionZ + this.viewDistance + 1; ++dz) {
                            if (isChunkInRange(dx, dz, sectionX, sectionZ, this.viewDistance)) {
                                long pos = ChunkPos.asLong(dx, dz);
                                if (!this.chunksLoaded.contains(pos)) {
                                    if (chunksToLoad.add(pos)) {
                                        chunksToUnload.remove(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //Tickets
        if (shouldRemovePlayerTicket) {
            if (shouldCameraLoad && !shouldUnloadAll && lastSectionPos.asLong() == currentCameraSectionPos.asLong()) {
                shouldRemovePlayerTicket = false;
            }
        }
        if (shouldRemoveCameraTicket) {
            if (lastCameraSectionPos.asLong() == currentSectionPos.asLong()) {
                shouldRemoveCameraTicket = false;
            }
        }
        if (shouldRemovePlayerTicket) {
            this.distanceManager.removePlayer(lastSectionPos, player);
        }
        if (shouldRemoveCameraTicket) {
            this.distanceManager.removePlayer(lastCameraSectionPos, player);
        }
        if (shouldAddPlayerTicket) {
            this.distanceManager.addPlayer(currentSectionPos, player);
        }
        if (shouldAddCameraTicket) {
            this.distanceManager.addPlayer(currentCameraSectionPos, player);
        }
        for (LSet.Entry e = chunksToUnload.fastEntries(); e != null; e = chunksToUnload.fastEntries()) {
            this.updateChunkTrackingNoPkt(player, e.get(), true, false);
        }
        for (LSet.Entry e = chunksToLoad.fastEntries(); e != null; e = chunksToLoad.fastEntries()) {
            this.updateChunkTrackingNoPkt(player, e.get(), false, true);
        }
    }

    @Shadow
    protected abstract boolean playerIsCloseEnoughForSpawning(ServerPlayer serverPlayer, ChunkPos chunkPos);

    @Shadow
    protected abstract void playerLoadedChunk(ServerPlayer serverPlayer,
                                              MutableObject<ClientboundLevelChunkWithLightPacket> mutableObject,
                                              LevelChunk levelChunk);

    private void playerLoadedChunkNoPkt(ServerPlayer player, LevelChunk chunk) {
        player.trackChunk(chunk.getPos(), new ClientboundLevelChunkWithLightPacket(chunk, this.lightEngine, null, null, true));
        OList<Mob> leashes = null;
        OList<Entity> passengers = null;
        for (ChunkMap.TrackedEntity trackedEntity : this.entityMap.values()) {
            Entity entity = trackedEntity.entity;
            if (entity != player && entity.chunkPosition().equals(chunk.getPos())) {
                trackedEntity.updatePlayer(player);
                if (entity instanceof Mob mob && mob.getLeashHolder() != null) {
                    if (leashes == null) {
                        leashes = new OArrayList<>();
                    }
                    leashes.add(mob);
                }
                if (!entity.getPassengers().isEmpty()) {
                    if (passengers == null) {
                        passengers = new OArrayList<>();
                    }
                    passengers.add(entity);
                }
            }
        }
        if (leashes != null) {
            for (Mob mob : leashes) {
                //noinspection ObjectAllocationInLoop
                player.connection.send(new ClientboundSetEntityLinkPacket(mob, mob.getLeashHolder()));
            }
        }
        if (passengers != null) {
            for (Entity passenger : passengers) {
                //noinspection ObjectAllocationInLoop
                player.connection.send(new ClientboundSetPassengersPacket(passenger));
            }
        }
    }

    @Overwrite
    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareTickingChunk(ChunkHolder holder) {
        ChunkPos chunkPos = holder.getPos();
        CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> future = this.getChunkRangeFuture(chunkPos, 1,
                                                                                                                        i -> ChunkStatus.FULL);
        CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> processingFuture = future.thenApplyAsync(either -> {
            return either.mapLeft(list -> {
                return (LevelChunk) list.get(list.size() / 2);
            });
        }, runnable -> {
            this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(holder, runnable));
        });
        processingFuture.thenAcceptAsync(either -> {
            PatchEither<LevelChunk, ChunkHolder.ChunkLoadingFailure> e = (PatchEither<LevelChunk, ChunkHolder.ChunkLoadingFailure>) either;
            if (e.isLeft()) {
                LevelChunk chunk = e.getLeft();
                chunk.postProcessGeneration();
                this.level.startTickingChunk(chunk);
            }
        }, this.mainThreadExecutor);
        processingFuture.thenAcceptAsync(either -> {
            PatchEither<LevelChunk, ChunkHolder.ChunkLoadingFailure> e = (PatchEither<LevelChunk, ChunkHolder.ChunkLoadingFailure>) either;
            if (e.isLeft()) {
                this.tickingGenerated.getAndIncrement();
                LevelChunk chunk = e.getLeft();
                List<ServerPlayer> players = this.getPlayers(chunk.getPos(), false);
                if (!players.isEmpty()) {
                    MutableObject<ClientboundLevelChunkWithLightPacket> mutableObject = new MutableObject();
                    for (int i = 0, len = players.size(); i < len; ++i) {
                        this.playerLoadedChunk(players.get(i), mutableObject, chunk);
                    }
                }
            }
        }, runnable -> {
            this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(holder, runnable));
        });
        return processingFuture;
    }

    @Shadow
    protected abstract void processUnloads(BooleanSupplier pHasMoreTime);

    /**
     * @author TheGreatWolf
     * @reason Fix MC-224729
     */
    @Overwrite
    private boolean save(ChunkAccess chunk) {
        this.poiManager.flush(chunk.getPos());
        if (!chunk.isUnsaved()) {
            return false;
        }
        chunk.setUnsaved(false);
        ChunkPos pos = chunk.getPos();
        try {
            ChunkStatus status = chunk.getStatus();
            if (status.getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK) {
                if (this.isExistingChunkFull(pos)) {
                    return false;
                }
                if (status == ChunkStatus.EMPTY && chunk.getAllStarts().values().stream().noneMatch(StructureStart::isValid)) {
                    return false;
                }
            }
            this.level.getProfiler().incrementCounter("chunkSave");
            CompoundTag compoundtag = ChunkSerializer.write(this.level, chunk);
            //Save event
            this.write(pos, compoundtag);
            this.markPosition(pos, status.getChunkType());
            return true;
        }
        catch (Exception exception) {
            LOGGER.error("Failed to save chunk {},{}", pos.x, pos.z, exception);
            return false;
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Fix MC-224729
     */
    @Overwrite
    public void saveAllChunks(boolean flush) {
        if (flush) {
            List<ChunkHolder> list = this.visibleChunkMap.values()
                                                         .stream()
                                                         //Remove filter to make it always accessible flush save
                                                         .peek(ChunkHolder::refreshAccessibility)
                                                         .collect(Collectors.toList());
            MutableBoolean mutableboolean = new MutableBoolean();
            do {
                mutableboolean.setFalse();
                //noinspection ObjectAllocationInLoop
                list.stream().map(p_203102_ -> {
                    CompletableFuture<ChunkAccess> completablefuture;
                    do {
                        completablefuture = p_203102_.getChunkToSave();
                        //noinspection ObjectAllocationInLoop
                        this.mainThreadExecutor.managedBlock(completablefuture::isDone);
                    } while (completablefuture != p_203102_.getChunkToSave());
                    return completablefuture.join();
                }).filter(c -> c instanceof LevelChunk /*save proto chunks*/ || c instanceof ProtoChunk).filter(this::save).forEach(
                        p_203051_ -> mutableboolean.setTrue());
            } while (mutableboolean.isTrue());
            this.processUnloads(() -> true);
            this.flushWorker();
        }
        else {
            this.visibleChunkMap.values().forEach(this::saveChunkIfNeeded);
        }
    }

    @Shadow
    protected abstract boolean saveChunkIfNeeded(ChunkHolder p_198875_);

    /**
     * @author TheGreatWolf
     * @reason Avoid (most) allocations, handle when the camera is not the player
     */
    @Overwrite
    public void setViewDistance(int viewDistance) {
        int clamppedViewDist = Mth.clamp(viewDistance + 1, 3, 33);
        if (clamppedViewDist != this.viewDistance) {
            int oldViewDist = this.viewDistance;
            this.viewDistance = clamppedViewDist;
            this.distanceManager.updatePlayerTickets(this.viewDistance + 1);
            for (ChunkHolder chunkHolder : this.updatingChunkMap.values()) {
                ChunkPos pos = chunkHolder.getPos();
                //noinspection ObjectAllocationInLoop
                MutableObject<ClientboundLevelChunkWithLightPacket> packetHolder = new MutableObject<>();
                for (ServerPlayer player : this.getPlayers(pos, false)) {
                    SectionPos sectionPos = player.getLastSectionPos();
                    boolean wasLoaded = isChunkInRange(pos.x, pos.z, sectionPos.x(), sectionPos.z(), oldViewDist);
                    boolean load = isChunkInRange(pos.x, pos.z, sectionPos.x(), sectionPos.z(), this.viewDistance);
                    this.updateChunkTracking(player, pos, packetHolder, wasLoaded, load);
                }
            }
        }
    }

    @Shadow
    protected abstract boolean skipPlayer(ServerPlayer pPlayer);

    @Shadow
    protected abstract void updateChunkTracking(ServerPlayer pPlayer,
                                                ChunkPos pChunkPos,
                                                MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache,
                                                boolean pWasLoaded,
                                                boolean pLoad);

    @Unique
    private void updateChunkTrackingNoPkt(ServerPlayer player, long pos, boolean wasLoaded, boolean load) {
        if (player.level == this.level) {
            //Chunk Watch event
            if (load && !wasLoaded) {
                ChunkHolder chunkholder = this.getVisibleChunkIfPresent(pos);
                if (chunkholder != null) {
                    LevelChunk chunk = chunkholder.getTickingChunk();
                    if (chunk != null) {
                        this.playerLoadedChunkNoPkt(player, chunk);
                    }
                }
            }
            else if (!load && wasLoaded) {
                if (player.isAlive()) {
                    player.connection.send(new ClientboundForgetLevelChunkPacket(ChunkPos.getX(pos), ChunkPos.getZ(pos)));
                }
            }
        }
    }

    @Shadow
    protected abstract SectionPos updatePlayerPos(ServerPlayer p_140374_);

    @Overwrite
    public void updatePlayerStatus(ServerPlayer player, boolean load) {
        boolean skip = this.skipPlayer(player);
        boolean ignored = this.playerMap.ignoredOrUnknown(player);
        BlockPos pos = player.blockPosition();
        int secX = SectionPos.blockToSectionCoord(pos.getX());
        int secZ = SectionPos.blockToSectionCoord(pos.getZ());
        if (load) {
            this.playerMap.addPlayer(ChunkPos.asLong(secX, secZ), player, skip);
            this.updatePlayerPos(player);
            if (!skip) {
                this.distanceManager.addPlayer(SectionPos.of(player), player);
            }
        }
        else {
            SectionPos sectionPos = player.getLastSectionPos();
            this.playerMap.removePlayer(ChunkPos.asLong(sectionPos.x(), sectionPos.z()), player);
            if (!ignored) {
                this.distanceManager.removePlayer(sectionPos, player);
            }
        }
        int x0 = secX - this.viewDistance - 1;
        int x1 = secX + this.viewDistance + 1;
        int z0 = secZ - this.viewDistance - 1;
        int z1 = secZ + this.viewDistance + 1;
        for (int dx = x0; dx <= x1; ++dx) {
            for (int dz = z0; dz <= z1; ++dz) {
                if (isChunkInRange(dx, dz, secX, secZ, this.viewDistance)) {
                    this.updateChunkTrackingNoPkt(player, ChunkPos.asLong(dx, dz), !load, load);
                }
            }
        }
    }
}
