package tgw.evolution.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Queues;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.*;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.network.PacketSCUpdateCameraViewCenter;
import tgw.evolution.patches.PatchChunkMap;
import tgw.evolution.patches.PatchEither;
import tgw.evolution.util.OptionalMutableChunkPos;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.*;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.collection.sets.LSet;
import tgw.evolution.util.physics.EarthHelper;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

@Mixin(ChunkMap.class)
public abstract class Mixin_CFM_ChunkMap extends ChunkStorage implements PatchChunkMap, ChunkHolder.PlayerProvider {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final public static int MAX_CHUNK_DISTANCE;
    @DeleteField @Shadow @Final private Long2LongMap chunkSaveCooldowns;
    @Unique private final L2LMap chunkSaveCooldowns_;
    @Mutable @Shadow @Final @RestoreFinal private ChunkStatusUpdateListener chunkStatusListener;
    @DeleteField @Shadow @Final private Long2ByteMap chunkTypeCache;
    @Unique private final L2BMap chunkTypeCache_;
    @Unique private final LSet chunksLoaded;
    @Unique private final LSet chunksToLoad;
    @Unique private final LSet chunksToUnload;
    @Mutable @Shadow @Final @RestoreFinal private ChunkMap.DistanceManager distanceManager;
    @DeleteField @Shadow @Final private LongSet entitiesInLevel;
    @Unique private final LSet entitiesInLevel_;
    @DeleteField @Shadow @Final private Int2ObjectMap<ChunkMap.TrackedEntity> entityMap;
    @Unique private final I2OMap<ChunkMap.TrackedEntity> entityMap_;
    @Shadow private ChunkGenerator generator;
    @Mutable @Shadow @Final @RestoreFinal ServerLevel level;
    @Mutable @Shadow @Final @RestoreFinal private ThreadedLevelLightEngine lightEngine;
    @Mutable @Shadow @Final @RestoreFinal public BlockableEventLoop<Runnable> mainThreadExecutor;
    @Mutable @Shadow @Final @RestoreFinal private ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailbox;
    @Shadow private boolean modified;
    @Mutable @Shadow @Final @RestoreFinal private Supplier<DimensionDataStorage> overworldDataStorage;
    @DeleteField @Shadow @Final private Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads;
    @Unique private final L2OMap<ChunkHolder> pendingUnloads_;
    @Mutable @Shadow @Final @RestoreFinal private PlayerMap playerMap;
    @Mutable @Shadow @Final @RestoreFinal private PoiManager poiManager;
    @Mutable @Shadow @Final @RestoreFinal private ChunkProgressListener progressListener;
    @Mutable @Shadow @Final @RestoreFinal private ChunkTaskPriorityQueueSorter queueSorter;
    @Mutable @Shadow @Final @RestoreFinal private String storageName;
    @Mutable @Shadow @Final @RestoreFinal private StructureManager structureManager;
    @Mutable @Shadow @Final @RestoreFinal private AtomicInteger tickingGenerated;
    @Mutable @Shadow @Final @RestoreFinal LongSet toDrop;
    @Mutable @Shadow @Final @RestoreFinal private Queue<Runnable> unloadQueue;
    @DeleteField @Shadow @Final private Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap;
    @Unique private final L2OLinkedHashMap<ChunkHolder> updatingChunkMap_;
    @Shadow public int viewDistance;
    @DeleteField @Shadow private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap;
    @Unique private volatile L2OMap<ChunkHolder> visibleChunkMap_;
    @Mutable @Shadow @Final @RestoreFinal private ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> worldgenMailbox;

    @ModifyConstructor
    public Mixin_CFM_ChunkMap(ServerLevel serverLevel, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, StructureManager structureManager, Executor executor, BlockableEventLoop<Runnable> blockableEventLoop, LightChunkGetter lightChunkGetter, ChunkGenerator chunkGenerator, ChunkProgressListener chunkProgressListener, ChunkStatusUpdateListener chunkStatusUpdateListener, Supplier<DimensionDataStorage> supplier, int i, boolean bl) {
        super(levelStorageAccess.getDimensionPath(serverLevel.dimension()).resolve("region"), dataFixer, bl);
        this.updatingChunkMap_ = new L2OLinkedHashMap<>();
        this.chunksLoaded = new LHashSet();
        this.chunksToLoad = new LHashSet();
        this.chunksToUnload = new LHashSet();
        this.visibleChunkMap_ = new L2OLinkedHashMap<>();
        this.pendingUnloads_ = new L2OLinkedHashMap<>();
        this.entitiesInLevel_ = new LHashSet();
        this.toDrop = new LHashSet();
        this.tickingGenerated = new AtomicInteger();
        this.playerMap = new PlayerMap();
        this.entityMap_ = new I2OHashMap<>();
        this.chunkTypeCache_ = new L2BHashMap();
        this.chunkSaveCooldowns_ = new L2LHashMap();
        this.unloadQueue = Queues.newConcurrentLinkedQueue();
        this.structureManager = structureManager;
        Path path = levelStorageAccess.getDimensionPath(serverLevel.dimension());
        this.storageName = path.getFileName().toString();
        this.level = serverLevel;
        this.generator = chunkGenerator;
        this.mainThreadExecutor = blockableEventLoop;
        ProcessorMailbox<Runnable> processorMailbox = ProcessorMailbox.create(executor, "worldgen");
        Objects.requireNonNull(blockableEventLoop);
        ProcessorHandle<Runnable> processorHandle = ProcessorHandle.of("main", blockableEventLoop::tell);
        this.progressListener = chunkProgressListener;
        this.chunkStatusListener = chunkStatusUpdateListener;
        ProcessorMailbox<Runnable> lightMailbox = ProcessorMailbox.create(executor, "light");
        this.queueSorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(processorMailbox, processorHandle, lightMailbox), executor, Integer.MAX_VALUE);
        this.worldgenMailbox = this.queueSorter.getProcessor(processorMailbox, false);
        this.mainThreadMailbox = this.queueSorter.getProcessor(processorHandle, false);
        this.lightEngine = new ThreadedLevelLightEngine(lightChunkGetter, (ChunkMap) (Object) this, this.level.dimensionType().hasSkyLight(), lightMailbox, this.queueSorter.getProcessor(lightMailbox, false));
        this.distanceManager = ((ChunkMap) (Object) this).new DistanceManager(executor, blockableEventLoop);
        this.overworldDataStorage = supplier;
        this.poiManager = new PoiManager(path.resolve("poi"), dataFixer, bl, serverLevel);
        this.setViewDistance(i);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static double euclideanDistanceSquared(ChunkPos chunkPos, Entity entity) {
        throw new AbstractMethodError();
    }

    @Unique
    private static double euclideanDistanceSquared(int chunkX, int chunkZ, Entity entity) {
        double x = SectionPos.sectionToBlockCoord(chunkX, 8);
        double z = SectionPos.sectionToBlockCoord(chunkZ, 8);
        double dx = EarthHelper.absDeltaBlockCoordinate(x, entity.getX());
        double dz = EarthHelper.absDeltaBlockCoordinate(z, entity.getZ());
        return dx * dx + dz * dz;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static boolean isChunkInRange(int x0, int z0, int x1, int z1, int range) {
        int dx = Math.max(0, EarthHelper.absDeltaChunkCoordinate(x0, x1) - 1);
        int dz = Math.max(0, EarthHelper.absDeltaChunkCoordinate(z0, z1) - 1);
        long dMax = Math.max(0, Math.max(dx, dz) - 1);
        long dMin = Math.min(dx, dz);
        long dSqr = dMin * dMin + dMax * dMax;
        int r = range - 1;
        int rSqr = r * r;
        return dSqr <= rSqr;
    }

    @Contract(value = "_, _, _, _, _ -> _")
    @Shadow
    private static boolean isChunkOnRangeBorder(int p_183829_, int p_183830_, int p_183831_, int p_183832_, int p_183833_) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Unique
    private static boolean playerIsCloseEnoughForSpawning(ServerPlayer player, int chunkX, int chunkZ) {
        if (player.isSpectator()) {
            return false;
        }
        double d = euclideanDistanceSquared(chunkX, chunkZ, player);
        return d < 128 * 128;
    }

    @Shadow
    private static void postLoadProtoChunk(ServerLevel level, List<CompoundTag> list) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private static String printFuture(CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> future) {
        try {
            PatchEither<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = (PatchEither<LevelChunk, ChunkHolder.ChunkLoadingFailure>) future.getNow(null);
            return either != null ? either.isLeft() ? "done" : "unloaded" : "not completed";
        }
        catch (CompletionException e) {
            return "failed " + e.getCause().getMessage();
        }
        catch (CancellationException e) {
            return "cancelled";
        }
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
    public void addEntity(Entity entity) {
        if (!(entity instanceof EnderDragonPart)) {
            EntityType<?> entityType = entity.getType();
            int range = entityType.clientTrackingRange() * 16;
            if (range != 0) {
                I2OMap<ChunkMap.TrackedEntity> entityMap = this.entityMap_;
                if (entityMap.containsKey(entity.getId())) {
                    throw Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
                }
                ChunkMap.TrackedEntity newTrackedEntity = ((ChunkMap) (Object) this).new TrackedEntity(entity, range, entityType.updateInterval(), entityType.trackDeltas());
                entityMap.put(entity.getId(), newTrackedEntity);
                newTrackedEntity.updatePlayers(this.level.players());
                if (entity instanceof ServerPlayer serverPlayer) {
                    this.updatePlayerStatus(serverPlayer, true);
                    for (long it = entityMap.beginIteration(); entityMap.hasNextIteration(it); it = entityMap.nextEntry(it)) {
                        ChunkMap.TrackedEntity trackedEntity = entityMap.getIterationValue(it);
                        if (trackedEntity.entity != serverPlayer) {
                            trackedEntity.updatePlayer(serverPlayer);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean anyPlayerCloseEnoughForSpawning(int chunkX, int chunkZ) {
        if (!this.distanceManager.hasPlayersNearby(ChunkPos.asLong(chunkX, chunkZ))) {
            return false;
        }
        O2ZMap<ServerPlayer> playerMap = this.playerMap.getPlayerMap();
        for (long it = playerMap.beginIteration(); playerMap.hasNextIteration(it); it = playerMap.nextEntry(it)) {
            if (playerIsCloseEnoughForSpawning(playerMap.getIterationKey(it), chunkX, chunkZ)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean anyPlayerCloseEnoughForSpawning(ChunkPos pos) {
        Evolution.deprecatedMethod();
        return this.anyPlayerCloseEnoughForSpawning(pos.x, pos.z);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void broadcast(Entity entity, Packet<?> packet) {
        ChunkMap.TrackedEntity trackedEntity = this.entityMap_.get(entity.getId());
        if (trackedEntity != null) {
            trackedEntity.broadcast(packet);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void broadcastAndSend(Entity entity, Packet<?> packet) {
        ChunkMap.TrackedEntity trackedEntity = this.entityMap_.get(entity.getId());
        if (trackedEntity != null) {
            trackedEntity.broadcastAndSend(packet);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public ReportedException debugFuturesAndCreateReportedException(IllegalStateException e, String details) {
        StringBuilder builder = new StringBuilder();
        builder.append("Updating:").append(System.lineSeparator());
        L2OMap<ChunkHolder> updatingChunkMap = this.updatingChunkMap_;
        for (long it = updatingChunkMap.beginIteration(); updatingChunkMap.hasNextIteration(it); it = updatingChunkMap.nextEntry(it)) {
            ChunkHolder holder = updatingChunkMap.getIterationValue(it);
            List<Pair<ChunkStatus, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>>> allFutures = holder.getAllFutures();
            for (int i = 0, len = allFutures.size(); i < len; ++i) {
                Pair<ChunkStatus, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> pair = allFutures.get(i);
                CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> future = pair.getSecond();
                if (future != null && future.isDone() && future.join() == null) {
                    builder.append(holder.getPos()).append(" - status: ").append(pair.getFirst()).append(" future: ").append(future).append(System.lineSeparator());
                }
            }
        }
        builder.append("Visible:").append(System.lineSeparator());
        L2OMap<ChunkHolder> visibleChunkMap = this.visibleChunkMap_;
        for (long it = visibleChunkMap.beginIteration(); visibleChunkMap.hasNextIteration(it); it = visibleChunkMap.nextEntry(it)) {
            ChunkHolder holder = visibleChunkMap.getIterationValue(it);
            List<Pair<ChunkStatus, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>>> allFutures = holder.getAllFutures();
            for (int i = 0, len = allFutures.size(); i < len; ++i) {
                Pair<ChunkStatus, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> pair = allFutures.get(i);
                CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> future = pair.getSecond();
                if (future != null && future.isDone() && future.join() == null) {
                    builder.append(holder.getPos()).append(" - status: ").append(pair.getFirst()).append(" future: ").append(future).append(System.lineSeparator());
                }
            }
        }
        CrashReport report = CrashReport.forThrowable(e, "Chunk loading");
        CrashReportCategory category = report.addCategory("Chunk loading");
        category.setDetail("Details", details);
        category.setDetail("Futures", builder);
        return new ReportedException(report);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void dumpChunks(Writer writer) throws IOException {
        CsvOutput csvOutput = CsvOutput.builder()
                                       .addColumn("x")
                                       .addColumn("z")
                                       .addColumn("level")
                                       .addColumn("in_memory")
                                       .addColumn("status")
                                       .addColumn("full_status")
                                       .addColumn("accessible_ready")
                                       .addColumn("ticking_ready")
                                       .addColumn("entity_ticking_ready")
                                       .addColumn("ticket")
                                       .addColumn("spawning")
                                       .addColumn("block_entity_count")
                                       .addColumn("ticking_ticket")
                                       .addColumn("ticking_level")
                                       .addColumn("block_ticks")
                                       .addColumn("fluid_ticks")
                                       .build(writer);
        TickingTracker tickingTracker = this.distanceManager.tickingTracker();
        L2OMap<ChunkHolder> visibleChunkMap = this.visibleChunkMap_;
        for (long it = visibleChunkMap.beginIteration(); visibleChunkMap.hasNextIteration(it); it = visibleChunkMap.nextEntry(it)) {
            long pos = visibleChunkMap.getIterationKey(it);
            int x = ChunkPos.getX(pos);
            int z = ChunkPos.getZ(pos);
            ChunkHolder chunkHolder = visibleChunkMap.getIterationValue(it);
            @Nullable ChunkAccess chunkAccess = chunkHolder.getLastAvailable();
            @Nullable LevelChunk chunk = chunkAccess instanceof LevelChunk c ? c : null;
            if (chunk == null) {
                csvOutput.writeRow(x,
                                   z,
                                   chunkHolder.getTicketLevel(),
                                   chunkAccess != null,
                                   chunkAccess == null ? null : chunkAccess.getStatus(),
                                   null,
                                   printFuture(chunkHolder.getFullChunkFuture()),
                                   printFuture(chunkHolder.getTickingChunkFuture()),
                                   printFuture(chunkHolder.getEntityTickingChunkFuture()),
                                   this.distanceManager.getTicketDebugString(pos),
                                   this.anyPlayerCloseEnoughForSpawning(x, z),
                                   0,
                                   tickingTracker.getTicketDebugString(pos),
                                   tickingTracker.getLevel(pos),
                                   0,
                                   0
                );
            }
            else {
                csvOutput.writeRow(x,
                                   z,
                                   chunkHolder.getTicketLevel(),
                                   true,
                                   chunkAccess.getStatus(),
                                   chunk.getFullStatus(),
                                   printFuture(chunkHolder.getFullChunkFuture()),
                                   printFuture(chunkHolder.getTickingChunkFuture()),
                                   printFuture(chunkHolder.getEntityTickingChunkFuture()),
                                   this.distanceManager.getTicketDebugString(pos),
                                   this.anyPlayerCloseEnoughForSpawning(x, z),
                                   chunk.blockEntities_().size(),
                                   tickingTracker.getTicketDebugString(pos),
                                   tickingTracker.getLevel(pos),
                                   chunk.getBlockTicks().count(),
                                   chunk.getFluidTicks().count()
                );
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> getChunkRangeFuture(ChunkPos pos, int range, IntFunction<ChunkStatus> dependencyStatus) {
        OList<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> futureList = new OArrayList<>();
        OList<ChunkHolder> holderList = new OArrayList<>();
        int x0 = pos.x;
        int z0 = pos.z;
        for (int dz = -range; dz <= range; ++dz) {
            for (int dx = -range; dx <= range; ++dx) {
                int radius = Math.max(Math.abs(dx), Math.abs(dz));
                final long packedPos = ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(x0 + dx), EarthHelper.wrapChunkCoordinate(z0 + dz));
                ChunkHolder chunkHolder = this.getUpdatingChunkIfPresent(packedPos);
                if (chunkHolder == null) {
                    return CompletableFuture.completedFuture(Either.right(new ChunkHolder.ChunkLoadingFailure() {
                        @Override
                        public String toString() {
                            return "Unloaded " + new ChunkPos(packedPos);
                        }
                    }));
                }
                ChunkStatus status = dependencyStatus.apply(radius);
                CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = chunkHolder.getOrScheduleFuture(status, (ChunkMap) (Object) this);
                holderList.add(chunkHolder);
                futureList.add(completableFuture);
            }
        }
        CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> future = Util.sequence(futureList).thenApply(list -> {
            OList<ChunkAccess> chunkList = new OArrayList<>();
            for (int i = 0, len = list.size(); i < len; ++i) {
                PatchEither<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = (PatchEither<ChunkAccess, ChunkHolder.ChunkLoadingFailure>) list.get(i);
                if (either == null) {
                    throw this.debugFuturesAndCreateReportedException(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                }
                if (either.isRight()) {
                    int finalI = i;
                    return Either.right(new ChunkHolder.ChunkLoadingFailure() {
                        @Override
                        public String toString() {
                            return "Unloaded " + new ChunkPos(EarthHelper.wrapChunkCoordinate(range + finalI % (x0 * 2 + 1)), EarthHelper.wrapChunkCoordinate(z0 + finalI / (x0 * 2 + 1))) + " " + either.getRight();
                        }
                    });
                }
                chunkList.add(either.getLeft());
            }
            return Either.left(chunkList);
        });
        for (int i = 0, len = holderList.size(); i < len; ++i) {
            //noinspection ObjectAllocationInLoop
            holderList.get(i).addSaveDependency("getChunkRangeFuture " + pos + " " + range, future);
        }
        return future;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public Iterable<ChunkHolder> getChunks() {
        return Iterables.unmodifiableIterable(this.visibleChunkMap_.values());
    }

    @Shadow
    protected abstract ChunkStatus getDependencyStatus(ChunkStatus chunkStatus, int i);

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

    @Override
    public OList<ServerPlayer> getPlayersCloseForSpawning(int chunkX, int chunkZ) {
        long pos = ChunkPos.asLong(chunkX, chunkZ);
        if (!this.distanceManager.hasPlayersNearby(pos)) {
            return OList.emptyList();
        }
        OList<ServerPlayer> list = OList.emptyList();
        O2ZMap<ServerPlayer> playerMap = this.playerMap.getPlayerMap();
        for (long it = playerMap.beginIteration(); playerMap.hasNextIteration(it); it = playerMap.nextEntry(it)) {
            ServerPlayer player = playerMap.getIterationKey(it);
            if (playerIsCloseEnoughForSpawning(player, chunkX, chunkZ)) {
                if (list.isEmpty()) {
                    //noinspection ObjectAllocationInLoop
                    list = new OArrayList<>();
                }
                list.add(player);
            }
        }
        return list.immutable();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public List<ServerPlayer> getPlayersCloseForSpawning(ChunkPos pos) {
        Evolution.deprecatedMethod();
        return this.getPlayersCloseForSpawning(pos.x, pos.z);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public @Nullable ChunkHolder getUpdatingChunkIfPresent(long pos) {
        return this.updatingChunkMap_.get(ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(ChunkPos.getX(pos)), EarthHelper.wrapChunkCoordinate(ChunkPos.getZ(pos))));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public @Nullable ChunkHolder getVisibleChunkIfPresent(long pos) {
        return this.visibleChunkMap_.get(ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(ChunkPos.getX(pos)), EarthHelper.wrapChunkCoordinate(ChunkPos.getZ(pos))));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean hasWork() {
        return this.lightEngine.hasLightWork() || !this.pendingUnloads_.isEmpty() || !this.updatingChunkMap_.isEmpty() || this.poiManager.hasWork() || !this.toDrop.isEmpty() || !this.unloadQueue.isEmpty() || this.queueSorter.hasWork() || this.distanceManager.hasTickets();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private boolean isExistingChunkFull(ChunkPos pos) {
        byte b = this.chunkTypeCache.get(pos.toLong());
        if (b != 0) {
            return b == 1;
        }
        CompoundTag compoundTag;
        try {
            compoundTag = this.readChunk(pos);
            if (compoundTag == null) {
                this.markPositionReplaceable(pos);
                return false;
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to read chunk {}: {}", pos, e);
            this.markPositionReplaceable(pos);
            return false;
        }
        ChunkStatus.ChunkType chunkType = ChunkSerializer.getChunkTypeFromTag(compoundTag);
        return this.markPosition(pos, chunkType) == 1;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private byte markPosition(ChunkPos pos, ChunkStatus.ChunkType chunkType) {
        return this.chunkTypeCache_.put(pos.toLong(), (byte) (chunkType == ChunkStatus.ChunkType.PROTOCHUNK ? -1 : 1));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void markPositionReplaceable(ChunkPos pos) {
        this.chunkTypeCache_.put(pos.toLong(), (byte) -1);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid a few allocations.
     */
    @Overwrite
    public void move(ServerPlayer player) {
        I2OMap<ChunkMap.TrackedEntity> entityMap = this.entityMap_;
        for (long it = entityMap.beginIteration(); entityMap.hasNextIteration(it); it = entityMap.nextEntry(it)) {
            ChunkMap.TrackedEntity trackedEntity = entityMap.getIterationValue(it);
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
        if (EarthHelper.absDeltaChunkCoordinate(lastX, currX) <= this.viewDistance * 2 && EarthHelper.absDeltaChunkCoordinate(lastZ, currZ) <= this.viewDistance * 2) {
            int minX = currX - this.viewDistance - 1;
            int minZ = currZ - this.viewDistance - 1;
            int maxX = currX + this.viewDistance + 1;
            int maxZ = currZ + this.viewDistance + 1;
            int dx = EarthHelper.deltaChunkCoordinate(currX, lastX);
            if (dx < 0) {
                maxX += dx;
            }
            else {
                minX += dx;
            }
            int dz = EarthHelper.deltaChunkCoordinate(currZ, lastZ);
            if (dz < 0) {
                maxZ += dz;
            }
            else {
                minZ += dz;
            }
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    boolean wasLoaded = isChunkInRange(x, z, lastX, lastZ, this.viewDistance);
                    boolean load = isChunkInRange(x, z, currX, currZ, this.viewDistance);
                    if (load != wasLoaded) {
                        if (load) {
                            chunksToLoad.add(ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(x), EarthHelper.wrapChunkCoordinate(z)));
                        }
                        else {
                            chunksToUnload.add(ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(x), EarthHelper.wrapChunkCoordinate(z)));
                        }
                    }
                    else if (load) {
                        this.chunksLoaded.add(ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(x), EarthHelper.wrapChunkCoordinate(z)));
                    }
                }
            }
        }
        else {
            //Unloads chunks
            for (int dx = lastX - this.viewDistance - 1; dx <= lastX + this.viewDistance + 1; ++dx) {
                for (int dz = lastZ - this.viewDistance - 1; dz <= lastZ + this.viewDistance + 1; ++dz) {
                    if (isChunkInRange(dx, dz, lastX, lastZ, this.viewDistance)) {
                        chunksToUnload.add(ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(dx), EarthHelper.wrapChunkCoordinate(dz)));
                    }
                }
            }
            //Loads chunks
            for (int dx = currX - this.viewDistance - 1; dx <= currX + this.viewDistance + 1; ++dx) {
                for (int dz = currZ - this.viewDistance - 1; dz <= currZ + this.viewDistance + 1; ++dz) {
                    if (isChunkInRange(dx, dz, currX, currZ, this.viewDistance)) {
                        chunksToLoad.add(ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(dx), EarthHelper.wrapChunkCoordinate(dz)));
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
                int minX = currX - this.viewDistance - 1;
                int minZ = currZ - this.viewDistance - 1;
                int maxX = currX + this.viewDistance + 1;
                int maxZ = currZ + this.viewDistance + 1;
                int dx = EarthHelper.deltaChunkCoordinate(currX, lastX);
                if (dx < 0) {
                    maxX += dx;
                }
                else {
                    minX += dx;
                }
                int dz = EarthHelper.deltaChunkCoordinate(currZ, lastZ);
                if (dz < 0) {
                    maxZ += dz;
                }
                else {
                    minZ += dz;
                }
                for (int x = minX; x <= maxX; ++x) {
                    for (int z = minZ; z <= maxZ; ++z) {
                        boolean wasLoaded = isChunkInRange(x, z, lastX, lastZ, this.viewDistance);
                        boolean load = isChunkInRange(x, z, currX, currZ, this.viewDistance);
                        long pos = ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(x), EarthHelper.wrapChunkCoordinate(z));
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
                            long pos = ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(dx), EarthHelper.wrapChunkCoordinate(dz));
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
                                long pos = ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(dx), EarthHelper.wrapChunkCoordinate(dz));
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
     * @author TheGreatWolf
     * @reason _
     */
    @DeleteMethod
    @Overwrite
    private boolean playerIsCloseEnoughForSpawning(ServerPlayer serverPlayer, ChunkPos chunkPos) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void playerLoadedChunk(ServerPlayer player, MutableObject<ClientboundLevelChunkWithLightPacket> packetHolder, LevelChunk chunk) {
        if (packetHolder.getValue() == null) {
            packetHolder.setValue(new ClientboundLevelChunkWithLightPacket(chunk, this.lightEngine, null, null, true));
        }
        player.trackChunk(chunk.getPos(), packetHolder.getValue());
        this.updateLeashedAndPassengers(player, chunk);
    }

    @Unique
    private void playerLoadedChunkNoPkt(ServerPlayer player, LevelChunk chunk) {
        player.trackChunk(chunk.getPos(), new ClientboundLevelChunkWithLightPacket(chunk, this.lightEngine, null, null, true));
        this.updateLeashedAndPassengers(player, chunk);
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
    private void processUnloads(BooleanSupplier hasTime) {
        ProfilerFiller profiler = this.level.getProfiler();
        profiler.push("scheduleUnload");
        LSet toDrop = (LSet) this.toDrop;
        int i = 0;
        for (long it = toDrop.beginIteration(); toDrop.hasNextIteration(it) && (hasTime.getAsBoolean() || i < 200 || toDrop.size() > 2_000); it = toDrop.nextEntry(it)) {
            long l = toDrop.getIteration(it);
            ChunkHolder chunkHolder = this.updatingChunkMap_.remove(l);
            if (chunkHolder != null) {
                this.pendingUnloads_.put(l, chunkHolder);
                this.modified = true;
                ++i;
                this.scheduleUnload(l, chunkHolder);
            }
            it = toDrop.removeIteration(it);
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
        L2OMap<ChunkHolder> visibleChunkMap = this.visibleChunkMap_;
        for (long it = visibleChunkMap.beginIteration(); k < 20 && hasTime.getAsBoolean() && visibleChunkMap.hasNextIteration(it); it = visibleChunkMap.nextEntry(it)) {
            if (this.saveChunkIfNeeded(visibleChunkMap.getIterationValue(it))) {
                ++k;
            }
        }
        profiler.pop();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean promoteChunkMap() {
        if (!this.modified) {
            return false;
        }
        this.visibleChunkMap_ = this.updatingChunkMap_.clone();
        this.modified = false;
        return true;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> protoChunkToFullChunk(ChunkHolder holder) {
        return holder.getFutureIfPresentUnchecked(ChunkStatus.FULL.getParent()).thenApplyAsync(either -> {
            ChunkStatus status = ChunkHolder.getStatus(holder.getTicketLevel());
            return !status.isOrAfter(ChunkStatus.FULL) ? ChunkHolder.UNLOADED_CHUNK : either.mapLeft(chunk -> {
                ChunkPos chunkPos = holder.getPos();
                ProtoChunk protoChunk = (ProtoChunk) chunk;
                LevelChunk levelChunk;
                if (protoChunk instanceof ImposterProtoChunk ipc) {
                    levelChunk = ipc.getWrapped();
                }
                else {
                    levelChunk = new LevelChunk(this.level, protoChunk, c -> postLoadProtoChunk(this.level, protoChunk.getEntities()));
                    holder.replaceProtoChunk(new ImposterProtoChunk(levelChunk, false));
                }
                levelChunk.setFullStatus(() -> ChunkHolder.getFullChunkStatus(holder.getTicketLevel()));
                levelChunk.runPostLoad();
                if (this.entitiesInLevel_.add(chunkPos.toLong())) {
                    levelChunk.setLoaded(true);
                    levelChunk.registerAllBlockEntitiesAfterLevelLoad();
                    levelChunk.registerTickContainerInLevel(this.level);
                }
                return levelChunk;
            });
        }, runnable -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(runnable, holder.getPos().toLong(), holder::getTicketLevel)));
    }

    @Shadow
    protected abstract @Nullable CompoundTag readChunk(ChunkPos chunkPos) throws IOException;

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
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void removeEntity(Entity entity) {
        I2OMap<ChunkMap.TrackedEntity> entityMap = this.entityMap_;
        if (entity instanceof ServerPlayer serverPlayer) {
            this.updatePlayerStatus(serverPlayer, false);
            for (long it = entityMap.beginIteration(); entityMap.hasNextIteration(it); it = entityMap.nextEntry(it)) {
                entityMap.getIterationValue(it).removePlayer(serverPlayer);
            }
        }
        ChunkMap.TrackedEntity trackedEntity = entityMap.remove(entity.getId());
        if (trackedEntity != null) {
            trackedEntity.broadcastRemoved();
        }
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
     * @reason Fix MC-224729
     */
    @Overwrite
    public void saveAllChunks(boolean flush) {
        L2OMap<ChunkHolder> visibleChunkMap = this.visibleChunkMap_;
        if (flush) {
            OList<ChunkHolder> list = new OArrayList<>(visibleChunkMap.size());
            for (long it = visibleChunkMap.beginIteration(); visibleChunkMap.hasNextIteration(it); it = visibleChunkMap.nextEntry(it)) {
                ChunkHolder holder = visibleChunkMap.getIterationValue(it);
                holder.refreshAccessibility();
                list.add(holder);
            }
            boolean saved = true;
            while (saved) {
                saved = false;
                for (int i = 0, len = list.size(); i < len; ++i) {
                    ChunkHolder holder = list.get(i);
                    CompletableFuture<ChunkAccess> completablefuture;
                    while (true) {
                        completablefuture = holder.getChunkToSave();
                        //noinspection ObjectAllocationInLoop
                        this.mainThreadExecutor.managedBlock(completablefuture::isDone);
                        if (completablefuture == holder.getChunkToSave()) {
                            break;
                        }
                    }
                    ChunkAccess chunk = completablefuture.join();
                    if (chunk instanceof LevelChunk || chunk instanceof ProtoChunk) {
                        if (this.save(chunk)) {
                            saved = true;
                        }
                    }
                }
            }
            this.processUnloads(() -> true);
            this.flushWorker();
        }
        else {
            for (long it = visibleChunkMap.beginIteration(); visibleChunkMap.hasNextIteration(it); it = visibleChunkMap.nextEntry(it)) {
                this.saveChunkIfNeeded(visibleChunkMap.getIterationValue(it));
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private boolean saveChunkIfNeeded(ChunkHolder holder) {
        if (!holder.wasAccessibleSinceLastSave()) {
            return false;
        }
        ChunkAccess chunk = holder.getChunkToSave().getNow(null);
        if (!(chunk instanceof ImposterProtoChunk) && !(chunk instanceof LevelChunk)) {
            return false;
        }
        long p = chunk.getPos().toLong();
        long cooldown = this.chunkSaveCooldowns_.getOrDefault(p, -1L);
        long currentTime = System.currentTimeMillis();
        if (currentTime < cooldown) {
            return false;
        }
        boolean saved = this.save(chunk);
        holder.refreshAccessibility();
        if (saved) {
            this.chunkSaveCooldowns_.put(p, currentTime + 10_000L);
        }
        return saved;
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

    @Shadow
    protected abstract CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkLoad(ChunkPos chunkPos);

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void scheduleUnload(long pos, ChunkHolder holder) {
        CompletableFuture<ChunkAccess> chunkFuture = holder.getChunkToSave();
        Consumer<ChunkAccess> consumer = chunk -> {
            if (holder.getChunkToSave() != chunkFuture) {
                this.scheduleUnload(pos, holder);
            }
            else {
                if (this.pendingUnloads_.remove(pos, holder) && chunk != null) {
                    if (chunk instanceof LevelChunk c) {
                        c.setLoaded(false);
                    }
                    this.save(chunk);
                    if (this.entitiesInLevel_.remove(pos) && chunk instanceof LevelChunk c) {
                        this.level.unload(c);
                    }
                    ChunkPos p = chunk.getPos();
                    this.lightEngine.updateChunkStatus(p);
                    this.lightEngine.tryScheduleUpdate();
                    this.progressListener.onStatusChange(p, null);
                    this.chunkSaveCooldowns_.remove(p.toLong());
                }
            }
        };
        chunkFuture.thenAcceptAsync(consumer, this.unloadQueue::add).whenComplete((v, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Failed to save chunk {}: {}", holder.getPos(), throwable);
            }
        });
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
            L2OMap<ChunkHolder> updatingChunkMap = this.updatingChunkMap_;
            for (long it = updatingChunkMap.beginIteration(); updatingChunkMap.hasNextIteration(it); it = updatingChunkMap.nextEntry(it)) {
                ChunkPos pos = updatingChunkMap.getIterationValue(it).getPos();
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
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public int size() {
        return this.visibleChunkMap_.size();
    }

    @Shadow
    protected abstract boolean skipPlayer(ServerPlayer pPlayer);

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void tick() {
        OList<ServerPlayer> list = OList.emptyList();
        List<ServerPlayer> players = this.level.players();
        I2OMap<ChunkMap.TrackedEntity> entityMap = this.entityMap_;
        for (long it = entityMap.beginIteration(); entityMap.hasNextIteration(it); it = entityMap.nextEntry(it)) {
            ChunkMap.TrackedEntity trackedEntity = entityMap.getIterationValue(it);
            SectionPos lastSection = trackedEntity.lastSectionPos;
            SectionPos currentSection = SectionPos.of(trackedEntity.entity);
            boolean moved = !lastSection.equals(currentSection);
            if (moved) {
                trackedEntity.updatePlayers(players);
                Entity entity = trackedEntity.entity;
                if (entity instanceof ServerPlayer p) {
                    if (list.isEmpty()) {
                        //noinspection ObjectAllocationInLoop
                        list = new OArrayList<>();
                    }
                    list.add(p);
                }
                trackedEntity.lastSectionPos = currentSection;
            }
            if (moved || this.distanceManager.inEntityTickingRange(currentSection.chunk().toLong())) {
                trackedEntity.serverEntity.sendChanges();
            }
        }
        if (!list.isEmpty()) {
            for (long it = entityMap.beginIteration(); entityMap.hasNextIteration(it); it = entityMap.nextEntry(it)) {
                entityMap.getIterationValue(it).updatePlayers(list);
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public @Nullable ChunkHolder updateChunkScheduling(long pos, int ticketLevel, @Nullable ChunkHolder chunkHolder, int j) {
        if (j > MAX_CHUNK_DISTANCE && ticketLevel > MAX_CHUNK_DISTANCE) {
            return chunkHolder;
        }
        if (chunkHolder != null) {
            chunkHolder.setTicketLevel(ticketLevel);
            if (ticketLevel > MAX_CHUNK_DISTANCE) {
                this.toDrop.add(pos);
            }
            else {
                this.toDrop.remove(pos);
            }
        }
        if (ticketLevel <= MAX_CHUNK_DISTANCE && chunkHolder == null) {
            chunkHolder = this.pendingUnloads_.remove(pos);
            if (chunkHolder != null) {
                chunkHolder.setTicketLevel(ticketLevel);
            }
            else {
                chunkHolder = new ChunkHolder(new ChunkPos(pos), ticketLevel, this.level, this.lightEngine, this.queueSorter, this);
            }
            this.updatingChunkMap_.put(pos, chunkHolder);
            this.modified = true;
        }
        return chunkHolder;
    }

    @Shadow
    protected abstract void updateChunkTracking(ServerPlayer pPlayer, ChunkPos pChunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, boolean pWasLoaded, boolean pLoad);

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

    @Unique
    private void updateLeashedAndPassengers(ServerPlayer player, LevelChunk chunk) {
        OList<Mob> leashed = null;
        OList<Entity> passengers = null;
        I2OMap<ChunkMap.TrackedEntity> entityMap = this.entityMap_;
        for (long it = entityMap.beginIteration(); entityMap.hasNextIteration(it); it = entityMap.nextEntry(it)) {
            ChunkMap.TrackedEntity trackedEntity = entityMap.getIterationValue(it);
            Entity entity = trackedEntity.entity;
            if (entity != player && entity.chunkPosition().equals(chunk.getPos())) {
                trackedEntity.updatePlayer(player);
                if (entity instanceof Mob mob && mob.getLeashHolder() != null) {
                    if (leashed == null) {
                        leashed = new OArrayList<>();
                    }
                    leashed.add(mob);
                }
                if (!entity.getPassengers().isEmpty()) {
                    if (passengers == null) {
                        passengers = new OArrayList<>();
                    }
                    passengers.add(entity);
                }
            }
        }
        if (leashed != null) {
            for (int i = 0, len = leashed.size(); i < len; ++i) {
                Mob mob = leashed.get(i);
                //noinspection ObjectAllocationInLoop
                player.connection.send(new ClientboundSetEntityLinkPacket(mob, mob.getLeashHolder()));
            }
        }
        if (passengers != null) {
            for (int i = 0, len = passengers.size(); i < len; ++i) {
                //noinspection ObjectAllocationInLoop
                player.connection.send(new ClientboundSetPassengersPacket(passengers.get(i)));
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
                    this.updateChunkTrackingNoPkt(player, ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(dx), EarthHelper.wrapChunkCoordinate(dz)), !load, load);
                }
            }
        }
    }
}
