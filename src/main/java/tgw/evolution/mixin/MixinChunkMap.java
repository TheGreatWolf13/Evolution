package tgw.evolution.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.*;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.network.PacketSCUpdateCameraViewCenter;
import tgw.evolution.patches.PatchChunkMap;
import tgw.evolution.patches.PatchEither;
import tgw.evolution.util.OptionalMutableChunkPos;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2ZMap;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.collection.sets.LSet;

import java.nio.file.Path;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.IntFunction;

@Mixin(ChunkMap.class)
public abstract class MixinChunkMap extends ChunkStorage implements PatchChunkMap, ChunkHolder.PlayerProvider {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final public BlockableEventLoop<Runnable> mainThreadExecutor;
    @Shadow public int viewDistance;
    @Shadow @Final ServerLevel level;
    @Shadow @Final LongSet toDrop;
    @Unique private final LSet chunksLoaded = new LHashSet();
    @Unique private final LSet chunksToLoad = new LHashSet();
    @Unique private final LSet chunksToUnload = new LHashSet();
    @Shadow @Final private ChunkMap.DistanceManager distanceManager;
    @Shadow @Final private Int2ObjectMap<ChunkMap.TrackedEntity> entityMap;
    @Shadow private ChunkGenerator generator;
    @Shadow @Final private ThreadedLevelLightEngine lightEngine;
    @Shadow @Final private ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailbox;
    @Shadow private boolean modified;
    @Shadow @Final private Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads;
    @Shadow @Final private PlayerMap playerMap;
    @Shadow @Final private PoiManager poiManager;
    @Shadow @Final private ChunkProgressListener progressListener;
    @Shadow @Final private StructureManager structureManager;
    @Shadow @Final private AtomicInteger tickingGenerated;
    @Shadow @Final private Queue<Runnable> unloadQueue;
    @Shadow @Final private Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap;
    @Shadow private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap;
    @Shadow @Final private ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> worldgenMailbox;

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

    @Unique
    private static void updatePlayerPos_(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        int secX = SectionPos.blockToSectionCoord(pos.getX());
        int secZ = SectionPos.blockToSectionCoord(pos.getZ());
        player.setLastChunkPos_(secX, secZ);
        player.connection.send(new ClientboundSetChunkCacheCenterPacket(secX, secZ));
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean anyPlayerCloseEnoughForSpawning(ChunkPos chunkPos) {
        if (!this.distanceManager.hasPlayersNearby(chunkPos.toLong())) {
            return false;
        }
        O2ZMap<ServerPlayer> playerMap = this.playerMap.getPlayerMap();
        for (long it = playerMap.beginIteration(); playerMap.hasNextIteration(it); it = playerMap.nextEntry(it)) {
            if (this.playerIsCloseEnoughForSpawning(playerMap.getIterationKey(it), chunkPos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public List<ServerPlayer> getPlayers(ChunkPos pos, boolean boundaryOnly) {
        OList<ServerPlayer> list = null;
        int x = pos.x;
        int z = pos.z;
        O2ZMap<ServerPlayer> playerMap = this.playerMap.getPlayerMap();
        for (long it = playerMap.beginIteration(); playerMap.hasNextIteration(it); it = playerMap.nextEntry(it)) {
            ServerPlayer player = playerMap.getIterationKey(it);
            ChunkPos lastChunkPos = player.getLastChunkPos();
            int lastX = lastChunkPos.x;
            int lastZ = lastChunkPos.z;
            if (boundaryOnly && isChunkOnRangeBorder(x, z, lastX, lastZ, this.viewDistance) || !boundaryOnly && isChunkInRange(x, z, lastX, lastZ, this.viewDistance)) {
                if (list == null) {
                    list = new OArrayList<>();
                }
                list.add(player);
                continue;
            }
            ChunkPos lastCameraChunkPos = player.getLastCameraChunkPos().getOrNull();
            if (lastCameraChunkPos != null) {
                int lastCamX = lastCameraChunkPos.x;
                int lastCamZ = lastCameraChunkPos.z;
                if (boundaryOnly && isChunkOnRangeBorder(x, z, lastCamX, lastCamZ, this.viewDistance) || !boundaryOnly && isChunkInRange(x, z, lastCamX, lastCamZ, this.viewDistance)) {
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
    public abstract @Nullable ChunkHolder getVisibleChunkIfPresent(long p_140328_);

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
        final ChunkPos lastSectionPos = player.getLastChunkPos();
        final int lastSecX = lastSectionPos.x;
        final int lastSecZ = lastSectionPos.z;
        BlockPos playerPos = player.blockPosition();
        final int currSecX = SectionPos.blockToSectionCoord(playerPos.getX());
        final int currSecZ = SectionPos.blockToSectionCoord(playerPos.getZ());
        long lastChunk = ChunkPos.asLong(lastSecX, lastSecZ);
        long currentChunk = ChunkPos.asLong(currSecX, currSecZ);
        boolean ignored = this.playerMap.ignored(player);
        boolean skip = this.skipPlayer(player);
        boolean diffSections = lastSecX != currSecX || lastSecZ != currSecZ;
        boolean shouldRemovePlayerTicket = false;
        boolean shouldAddPlayerTicket = false;
        final BlockPos cameraPos = player.getCamera().blockPosition();
        final int currCamSecX = SectionPos.blockToSectionCoord(cameraPos.getX());
        final int currCamSecZ = SectionPos.blockToSectionCoord(cameraPos.getZ());
        if (diffSections || ignored != skip) {
            updatePlayerPos_(player);
            player.connection.send(new PacketSCUpdateCameraViewCenter(currCamSecX, currCamSecZ));
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
        int currX = currSecX;
        int currZ = currSecZ;
        int lastX = lastSecX;
        int lastZ = lastSecZ;
        if (Math.abs(lastX - currX) <= this.viewDistance * 2 && Math.abs(lastZ - currZ) <= this.viewDistance * 2) {
            int minX = Math.min(currX, lastX) - this.viewDistance - 1;
            int minZ = Math.min(currZ, lastZ) - this.viewDistance - 1;
            int maxX = Math.max(currX, lastX) + this.viewDistance + 1;
            int maxZ = Math.max(currZ, lastZ) + this.viewDistance + 1;
            for (int dx = minX; dx <= maxX; ++dx) {
                for (int dz = minZ; dz <= maxZ; ++dz) {
                    boolean wasLoaded = isChunkInRange(dx, dz, lastX, lastZ, this.viewDistance);
                    boolean load = isChunkInRange(dx, dz, currX, currZ, this.viewDistance);
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
            for (int dx = currX - this.viewDistance - 1; dx <= currX + this.viewDistance + 1; ++dx) {
                for (int dz = currZ - this.viewDistance - 1; dz <= currZ + this.viewDistance + 1; ++dz) {
                    if (isChunkInRange(dx, dz, currX, currZ, this.viewDistance)) {
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
        OptionalMutableChunkPos camPosHolder = player.getLastCameraChunkPos();
        ChunkPos lastCameraSectionPos = camPosHolder.getOrNull();
        int lastCamSecX;
        int lastCamSecZ;
        if (lastCameraSectionPos == null) {
            lastCamSecX = Integer.MAX_VALUE;
            lastCamSecZ = Integer.MAX_VALUE;
        }
        else {
            lastCamSecX = lastCameraSectionPos.x;
            lastCamSecZ = lastCameraSectionPos.z;
        }
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
                lastCamSecX = lastSecX;
                lastCamSecZ = lastSecZ;
                firstTick = true;
            }
            currX = currCamSecX;
            currZ = currCamSecZ;
            if (firstTick || lastCamSecX != currCamSecX || lastCamSecZ != currCamSecZ) {
                shouldAddCameraTicket = true;
                shouldRemoveCameraTicket = !firstTick;
                player.connection.send(new PacketSCUpdateCameraViewCenter(currX, currZ));
            }
            if (shouldUnloadAll) {
                camPosHolder.remove();
            }
            else {
                camPosHolder.set(currCamSecX, currCamSecZ);
            }
            lastX = lastCamSecX;
            lastZ = lastCamSecZ;
            if (!shouldUnloadAll && Math.abs(lastX - currX) <= this.viewDistance * 2 && Math.abs(lastZ - currZ) <= this.viewDistance * 2) {
                int minX = Math.min(currX, lastX) - this.viewDistance - 1;
                int minZ = Math.min(currZ, lastZ) - this.viewDistance - 1;
                int maxX = Math.max(currX, lastX) + this.viewDistance + 1;
                int maxZ = Math.max(currZ, lastZ) + this.viewDistance + 1;
                for (int dx = minX; dx <= maxX; ++dx) {
                    for (int dz = minZ; dz <= maxZ; ++dz) {
                        boolean wasLoaded = isChunkInRange(dx, dz, lastX, lastZ, this.viewDistance);
                        boolean load = isChunkInRange(dx, dz, currX, currZ, this.viewDistance);
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
                    for (int dx = currX - this.viewDistance - 1; dx <= currX + this.viewDistance + 1; ++dx) {
                        for (int dz = currZ - this.viewDistance - 1; dz <= currZ + this.viewDistance + 1; ++dz) {
                            if (isChunkInRange(dx, dz, currX, currZ, this.viewDistance)) {
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
            if (shouldCameraLoad && !shouldUnloadAll && lastSecX == currCamSecX && lastSecZ == currCamSecZ) {
                shouldRemovePlayerTicket = false;
            }
        }
        if (shouldRemoveCameraTicket) {
            if (lastCamSecX == currSecX && lastCamSecZ == currSecZ) {
                shouldRemoveCameraTicket = false;
            }
        }
        if (shouldRemovePlayerTicket) {
            this.distanceManager.removePlayer_(lastSecX, lastSecZ, player);
        }
        if (shouldRemoveCameraTicket) {
            this.distanceManager.removePlayer_(lastCamSecX, lastCamSecZ, player);
        }
        if (shouldAddPlayerTicket) {
            this.distanceManager.addPlayer_(currSecX, currSecZ, player);
        }
        if (shouldAddCameraTicket) {
            this.distanceManager.addPlayer_(currCamSecX, currCamSecZ, player);
        }
        for (long it = chunksToUnload.beginIteration(); chunksToUnload.hasNextIteration(it); it = chunksToUnload.nextEntry(it)) {
            this.updateChunkTrackingNoPkt(player, chunksToUnload.getIteration(it), true, false);
        }
        for (long it = chunksToLoad.beginIteration(); chunksToLoad.hasNextIteration(it); it = chunksToLoad.nextEntry(it)) {
            this.updateChunkTrackingNoPkt(player, chunksToLoad.getIteration(it), false, true);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareTickingChunk(ChunkHolder holder) {
        ChunkPos chunkPos = holder.getPos();
        CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> future = this.getChunkRangeFuture(chunkPos, 1, i -> ChunkStatus.FULL);
        CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> processingFuture = future.thenApplyAsync(either -> either.mapLeft(list -> (LevelChunk) list.get(list.size() / 2)), runnable -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(holder, runnable)));
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
                    MutableObject<ClientboundLevelChunkWithLightPacket> mutableObject = new MutableObject<>();
                    for (int i = 0, len = players.size(); i < len; ++i) {
                        this.playerLoadedChunk(players.get(i), mutableObject, chunk);
                    }
                }
            }
        }, runnable -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(holder, runnable)));
        return processingFuture;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void releaseLightTicket(ChunkPos chunkPos) {
        Evolution.deprecatedMethod();
        this.releaseLightTicket_(chunkPos.toLong());
    }

    @Override
    public void releaseLightTicket_(long chunkPos) {
        this.mainThreadExecutor.tell(Util.name(() -> this.distanceManager.removeTicket_(TicketType.LIGHT, chunkPos, 33 + ChunkStatus.getDistance(ChunkStatus.LIGHT), chunkPos), () -> "release light ticket " + chunkPos));
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
                                                         .toList();
            boolean bool = false;
            do {
                bool = false;
                for (int i = 0, len = list.size(); i < len; ++i) {
                    ChunkHolder holder = list.get(i);
                    CompletableFuture<ChunkAccess> completablefuture;
                    do {
                        completablefuture = holder.getChunkToSave();
                        //noinspection ObjectAllocationInLoop
                        this.mainThreadExecutor.managedBlock(completablefuture::isDone);
                    } while (completablefuture != holder.getChunkToSave());
                    ChunkAccess chunk = completablefuture.join();
                    if (chunk instanceof LevelChunk || chunk instanceof ProtoChunk) {
                        if (this.save(chunk)) {
                            bool = true;
                        }
                    }
                }
            } while (bool);
            this.processUnloads(() -> true);
            this.flushWorker();
        }
        else {
            this.visibleChunkMap.values().forEach(this::saveChunkIfNeeded);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> schedule(ChunkHolder holder, ChunkStatus status) {
        ChunkPos pos = holder.getPos();
        if (status == ChunkStatus.EMPTY) {
            return this.scheduleChunkLoad(pos);
        }
        if (status == ChunkStatus.LIGHT) {
            long longPos = pos.toLong();
            this.distanceManager.addTicket_(TicketType.LIGHT, longPos, 33 + ChunkStatus.getDistance(ChunkStatus.LIGHT), longPos);
        }
        ChunkAccess chunk = ((PatchEither<ChunkAccess, ChunkHolder.ChunkLoadingFailure>) holder.getOrScheduleFuture(status.getParent(), (ChunkMap) (Object) this).getNow(ChunkHolder.UNLOADED_CHUNK)).leftOrNull();
        if (chunk != null && chunk.getStatus().isOrAfter(status)) {
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = status.load(this.level, this.structureManager, this.lightEngine, chunkAccess -> this.protoChunkToFullChunk(holder), chunk);
            this.progressListener.onStatusChange(pos, status);
            return completableFuture;
        }
        return this.scheduleChunkGeneration(holder, status);
    }

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
                int x = pos.x;
                int z = pos.z;
                //noinspection ObjectAllocationInLoop
                MutableObject<ClientboundLevelChunkWithLightPacket> packetHolder = new MutableObject<>();
                for (ServerPlayer player : this.getPlayers(pos, false)) {
                    ChunkPos chunkPos = player.getLastChunkPos();
                    int secX = chunkPos.x;
                    int secZ = chunkPos.z;
                    boolean wasLoaded = isChunkInRange(x, z, secX, secZ, oldViewDist);
                    boolean load = isChunkInRange(x, z, secX, secZ, this.viewDistance);
                    this.updateChunkTracking(player, pos, packetHolder, wasLoaded, load);
                }
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void updatePlayerStatus(ServerPlayer player, boolean load) {
        boolean skip = this.skipPlayer(player);
        boolean ignored = this.playerMap.ignoredOrUnknown(player);
        BlockPos pos = player.blockPosition();
        int secX = SectionPos.blockToSectionCoord(pos.getX());
        int secZ = SectionPos.blockToSectionCoord(pos.getZ());
        if (load) {
            this.playerMap.addPlayer(ChunkPos.asLong(secX, secZ), player, skip);
            updatePlayerPos_(player);
            if (!skip) {
                this.distanceManager.addPlayer_(secX, secZ, player);
            }
        }
        else {
            ChunkPos chunkPos = player.getLastChunkPos();
            int lastSecX = chunkPos.x;
            int lastSecZ = chunkPos.z;
            this.playerMap.removePlayer(ChunkPos.asLong(lastSecX, lastSecZ), player);
            if (!ignored) {
                this.distanceManager.removePlayer_(lastSecX, lastSecZ, player);
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

    @Shadow
    protected abstract CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> getChunkRangeFuture(ChunkPos chunkPos,
                                                                                                                         int i,
                                                                                                                         IntFunction<ChunkStatus> intFunction);

    @Shadow
    protected abstract ChunkStatus getDependencyStatus(ChunkStatus chunkStatus, int i);

    @Shadow
    protected abstract boolean isExistingChunkFull(ChunkPos pChunkPos);

    @Shadow
    protected abstract byte markPosition(ChunkPos p_140230_, ChunkStatus.ChunkType p_140231_);

    @Shadow
    protected abstract boolean playerIsCloseEnoughForSpawning(ServerPlayer serverPlayer, ChunkPos chunkPos);

    @Shadow
    protected abstract void playerLoadedChunk(ServerPlayer serverPlayer, MutableObject<ClientboundLevelChunkWithLightPacket> mutableObject, LevelChunk levelChunk);

    @Shadow
    protected abstract CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> protoChunkToFullChunk(ChunkHolder chunkHolder);

    @Shadow
    protected abstract boolean saveChunkIfNeeded(ChunkHolder p_198875_);

    @Shadow
    protected abstract CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkLoad(ChunkPos chunkPos);

    @Shadow
    protected abstract void scheduleUnload(long l, ChunkHolder chunkHolder);

    @Shadow
    protected abstract boolean skipPlayer(ServerPlayer pPlayer);

    @Shadow
    protected abstract void updateChunkTracking(ServerPlayer pPlayer, ChunkPos pChunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, boolean pWasLoaded, boolean pLoad);

    @Unique
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

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void processUnloads(BooleanSupplier hasTime) {
        ProfilerFiller profiler = this.level.getProfiler();
        profiler.push("scheduleUnload");
        LongIterator longIterator = this.toDrop.iterator();
        for (int i = 0; longIterator.hasNext() && (hasTime.getAsBoolean() || i < 200 || this.toDrop.size() > 2_000); longIterator.remove()) {
            long l = longIterator.nextLong();
            ChunkHolder chunkHolder = this.updatingChunkMap.remove(l);
            if (chunkHolder != null) {
                this.pendingUnloads.put(l, chunkHolder);
                this.modified = true;
                ++i;
                this.scheduleUnload(l, chunkHolder);
            }
        }
        profiler.popPush("processUnloads");
        int j = Math.max(0, this.unloadQueue.size() - 2_000);
        Runnable runnable;
        while ((hasTime.getAsBoolean() || j > 0) && (runnable = this.unloadQueue.poll()) != null) {
            --j;
            runnable.run();
        }
        profiler.popPush("saveChunks");
        int k = 0;
        ObjectIterator<ChunkHolder> objectIterator = this.visibleChunkMap.values().iterator();
        while (k < 20 && hasTime.getAsBoolean() && objectIterator.hasNext()) {
            if (this.saveChunkIfNeeded(objectIterator.next())) {
                ++k;
            }
        }
        profiler.pop();
    }

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
     * @reason _
     */
    @Overwrite
    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkGeneration(ChunkHolder holder, ChunkStatus status) {
        ChunkPos chunkPos = holder.getPos();
        CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> future = this.getChunkRangeFuture(chunkPos, status.getRange(), i -> this.getDependencyStatus(status, i));
        this.level.getProfiler().incrementCounter(() -> "chunkGenerate " + status.getName());
        Executor executor = runnable -> this.worldgenMailbox.tell(ChunkTaskPriorityQueueSorter.message(holder, runnable));
        return future.thenComposeAsync(either -> either.map(list -> {
            try {
                CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = status.generate(executor, this.level, this.generator, this.structureManager, this.lightEngine, chunkAccess -> this.protoChunkToFullChunk(holder), list, false);
                this.progressListener.onStatusChange(chunkPos, status);
                return completableFuture;
            }
            catch (Exception t) {
                t.getStackTrace();
                CrashReport crashReport = CrashReport.forThrowable(t, "Exception generating new chunk");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Chunk to be generated");
                crashReportCategory.setDetail("Location", String.format("%d,%d", chunkPos.x, chunkPos.z));
                crashReportCategory.setDetail("Position hash", ChunkPos.asLong(chunkPos.x, chunkPos.z));
                crashReportCategory.setDetail("Generator", this.generator);
                this.mainThreadExecutor.execute(() -> {
                    throw new ReportedException(crashReport);
                });
                throw new ReportedException(crashReport);
            }
        }, chunkLoadingFailure -> {
            this.releaseLightTicket_(chunkPos.toLong());
            return CompletableFuture.completedFuture(Either.right(chunkLoadingFailure));
        }), executor);
    }

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

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private SectionPos updatePlayerPos(ServerPlayer player) {
        throw new AbstractMethodError();
    }
}
