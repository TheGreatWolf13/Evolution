package tgw.evolution.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchEither;
import tgw.evolution.patches.PatchServerChunkCache;
import tgw.evolution.util.collection.lists.custom.BiArrayList;
import tgw.evolution.util.math.FastRandom;
import tgw.evolution.util.physics.EarthHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.random.RandomGenerator;

@Mixin(ServerChunkCache.class)
public abstract class MixinServerChunkCache extends ChunkSource implements PatchServerChunkCache {

    @Unique private static final ThreadLocal<BiArrayList<LevelChunk, ChunkHolder>> BILIST = ThreadLocal.withInitial(BiArrayList::new);
    @Shadow @Final private static List<ChunkStatus> CHUNK_STATUSES;
    @Shadow @Final public ChunkMap chunkMap;
    @Shadow @Final private DistanceManager distanceManager;
    @Shadow @Final private ChunkAccess[] lastChunk;
    @Shadow @Final private long[] lastChunkPos;
    @Shadow @Final private ChunkStatus[] lastChunkStatus;
    @Shadow private long lastInhabitedUpdate;
    @Shadow private @Nullable NaturalSpawner.SpawnState lastSpawnState;
    @Shadow @Final ServerLevel level;
    @Shadow @Final Thread mainThread;
    @Shadow @Final private ServerChunkCache.MainThreadExecutor mainThreadProcessor;
    @Unique private final RandomGenerator randomForTicking = new FastRandom();
    @Shadow private boolean spawnEnemies;
    @Shadow private boolean spawnFriendlies;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public <T> void addRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object) {
        Evolution.deprecatedMethod();
        throw new RuntimeException("Use non-ChunkPos version!");
    }

    @Override
    public <T> void addRegionTicket_(TicketType<T> ticketType, long chunkPos, int level, long key) {
        this.distanceManager.addRegionTicket_(ticketType, chunkPos, level, key);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void blockChanged(BlockPos pos) {
        Evolution.deprecatedMethod();
        this.blockChanged_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void blockChanged_(int x, int y, int z) {
        int secX = EarthHelper.wrapChunkCoordinate(SectionPos.blockToSectionCoord(x));
        int secZ = EarthHelper.wrapChunkCoordinate(SectionPos.blockToSectionCoord(z));
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(ChunkPos.asLong(secX, secZ));
        if (chunkHolder != null) {
            chunkHolder.blockChanged_(x, y, z);
        }
    }

    @Shadow
    protected abstract boolean chunkAbsent(@Nullable ChunkHolder p_8417_, int p_8418_);

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public @Nullable ChunkAccess getChunk(int x0, int z0, ChunkStatus status, boolean forceLoad) {
        int x = EarthHelper.wrapChunkCoordinate(x0);
        int z = EarthHelper.wrapChunkCoordinate(z0);
        if (Thread.currentThread() != this.mainThread) {
            return CompletableFuture.supplyAsync(() -> this.getChunk(x, z, status, forceLoad), this.mainThreadProcessor).join();
        }
        ProfilerFiller profiler = this.level.getProfiler();
        profiler.incrementCounter("getChunk");
        long pos = ChunkPos.asLong(x, z);
        ChunkAccess chunk;
        for (int k = 0; k < 4; ++k) {
            if (pos == this.lastChunkPos[k] && status == this.lastChunkStatus[k]) {
                chunk = this.lastChunk[k];
                if (chunk != null || !forceLoad) {
                    return chunk;
                }
            }
        }
        profiler.incrementCounter("getChunkCacheMiss");
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> future = this.getChunkFutureMainThread(x, z, status, forceLoad);
        this.mainThreadProcessor.managedBlock(future::isDone);
        PatchEither<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = (PatchEither<ChunkAccess, ChunkHolder.ChunkLoadingFailure>) future.join();
        if (either.isLeft()) {
            chunk = either.getLeft();
            this.storeInCache(pos, chunk, status);
            return chunk;
        }
        if (forceLoad) {
            throw Util.pauseInIde(new IllegalStateException("Chunk not there when requested: " + either.getRight()));
        }
        return null;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public BlockGetter getChunkForLighting(int x, int z) {
        long pos = ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(x), EarthHelper.wrapChunkCoordinate(z));
        ChunkHolder holder = this.getVisibleChunkIfPresent(pos);
        if (holder == null) {
            return null;
        }
        List<ChunkStatus> chunkStatuses = CHUNK_STATUSES;
        for (int i = chunkStatuses.size() - 1; ; i--) {
            ChunkStatus status = chunkStatuses.get(i);
            ChunkAccess chunk = ((PatchEither<ChunkAccess, ChunkHolder.ChunkLoadingFailure>) holder.getFutureIfPresentUnchecked(status).getNow(ChunkHolder.UNLOADED_CHUNK)).leftOrNull();
            if (chunk != null) {
                return chunk;
            }
            if (status == ChunkStatus.LIGHT.getParent()) {
                return null;
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture(int x0, int z0, ChunkStatus status, boolean forceLoad) {
        int x = EarthHelper.wrapChunkCoordinate(x0);
        int z = EarthHelper.wrapChunkCoordinate(z0);
        if (Thread.currentThread() == this.mainThread) {
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> future = this.getChunkFutureMainThread(x, z, status, forceLoad);
            this.mainThreadProcessor.managedBlock(future::isDone);
            return future;
        }
        return CompletableFuture.supplyAsync(() -> this.getChunkFutureMainThread(x, z, status, forceLoad), this.mainThreadProcessor).thenCompose(Function.identity());
    }

    /**
     * @author TheGreatWolf
     * @reason Delay ChunkPos allocation
     */
    @Overwrite
    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFutureMainThread(int x, int z, ChunkStatus status, boolean load) {
        assert !EarthHelper.isChunkOutsideMapping(x, z);
        long longPos = ChunkPos.asLong(x, z);
        int ticketLevel = 33 + ChunkStatus.getDistance(status);
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent(longPos);
        if (load) {
            this.distanceManager.addTicket_(TicketType.UNKNOWN, longPos, ticketLevel, longPos);
            if (this.chunkAbsent(chunkholder, ticketLevel)) {
                ProfilerFiller profilerfiller = this.level.getProfiler();
                profilerfiller.push("chunkLoad");
                this.runDistanceManagerUpdates();
                chunkholder = this.getVisibleChunkIfPresent(longPos);
                profilerfiller.pop();
                if (this.chunkAbsent(chunkholder, ticketLevel)) {
                    throw Util.pauseInIde(new IllegalStateException("No chunk holder after ticket has been added"));
                }
            }
        }
        //noinspection ConstantConditions
        return this.chunkAbsent(chunkholder, ticketLevel) ? ChunkHolder.UNLOADED_CHUNK_FUTURE : chunkholder.getOrScheduleFuture(status, this.chunkMap);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public @Nullable LevelChunk getChunkNow(int x, int z) {
        if (Thread.currentThread() != this.mainThread) {
            return null;
        }
        this.level.getProfiler().incrementCounter("getChunkNow");
        long pos = ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(x), EarthHelper.wrapChunkCoordinate(z));
        for (int i = 0; i < 4; ++i) {
            if (pos == this.lastChunkPos[i] && this.lastChunkStatus[i] == ChunkStatus.FULL) {
                ChunkAccess chunk = this.lastChunk[i];
                return chunk instanceof LevelChunk c ? c : null;
            }
        }
        ChunkHolder holder = this.getVisibleChunkIfPresent(pos);
        if (holder == null) {
            return null;
        }
        PatchEither<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = (PatchEither<ChunkAccess, ChunkHolder.ChunkLoadingFailure>) holder.getFutureIfPresent(ChunkStatus.FULL).getNow(null);
        if (either == null) {
            return null;
        }
        ChunkAccess chunk = either.leftOrNull();
        if (chunk != null) {
            this.storeInCache(pos, chunk, ChunkStatus.FULL);
            if (chunk instanceof LevelChunk c) {
                return c;
            }
        }
        return null;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void getFullChunk(long pos, Consumer<LevelChunk> consumer) {
        ChunkHolder holder = this.getVisibleChunkIfPresent(pos);
        if (holder != null) {
            PatchEither<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = (PatchEither<LevelChunk, ChunkHolder.ChunkLoadingFailure>) holder.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK);
            if (either.isLeft()) {
                consumer.accept(either.getLeft());
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private @Nullable ChunkHolder getVisibleChunkIfPresent(long pos) {
        assert !EarthHelper.isChunkOutsideMapping(pos);
        return this.chunkMap.getVisibleChunkIfPresent(pos);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Override
    @Overwrite
    public boolean hasChunk(int x, int z) {
        ChunkHolder holder = this.getVisibleChunkIfPresent(ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(x), EarthHelper.wrapChunkCoordinate(z)));
        int ticket = 33 + ChunkStatus.getDistance(ChunkStatus.FULL);
        return !this.chunkAbsent(holder, ticket);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean isPositionTicking(long pos) {
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(pos);
        if (chunkHolder == null) {
            return false;
        }
        if (!this.level.shouldTickBlocksAt(pos)) {
            return false;
        }
        PatchEither<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = (PatchEither<LevelChunk, ChunkHolder.ChunkLoadingFailure>) chunkHolder.getTickingChunkFuture().getNow(null);
        return either != null && either.isLeft();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void onLightUpdate(LightLayer lightLayer, SectionPos pos) {
        Evolution.deprecatedMethod();
        this.onLightUpdate_(lightLayer, pos.x(), pos.y(), pos.z());
    }

    @Override
    public void onLightUpdate_(LightLayer lightLayer, int secX, int secY, int secZ) {
        this.mainThreadProcessor.execute(() -> {
            ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(ChunkPos.asLong(EarthHelper.wrapChunkCoordinate(secX), EarthHelper.wrapChunkCoordinate(secZ)));
            if (chunkHolder != null) {
                chunkHolder.sectionLightChanged(lightLayer, secY);
            }
        });
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public <T> void removeRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object) {
        Evolution.deprecatedMethod();
        throw new RuntimeException("Use non-ChunkPos version!");
    }

    @Override
    public <T> void removeRegionTicket_(TicketType<T> ticketType, long chunkPos, int level, long key) {
        this.distanceManager.removeRegionTicket_(ticketType, chunkPos, level, key);
    }

    @Shadow
    abstract boolean runDistanceManagerUpdates();

    @Shadow
    public abstract void setSimulationDistance(int i);

    @Shadow
    protected abstract void storeInCache(long l, ChunkAccess chunkAccess, ChunkStatus chunkStatus);

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    private void tickChunks() {
        long gameTime = this.level.getGameTime();
        long deltaTime = gameTime - this.lastInhabitedUpdate;
        this.lastInhabitedUpdate = gameTime;
        boolean debug = this.level.isDebug();
        if (debug) {
            this.chunkMap.tick();
        }
        else {
            LevelData leveldata = this.level.getLevelData();
            ProfilerFiller profiler = this.level.getProfiler();
            profiler.push("pollingChunks");
            int randomTickSpeed = this.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
            boolean shouldSpawnAnimals = leveldata.getGameTime() % 400L == 0L;
            profiler.push("naturalSpawnCount");
            int chunkCount = this.distanceManager.getNaturalSpawnChunkCount();
            NaturalSpawner.SpawnState state = NaturalSpawner.createState(chunkCount, this.level.getAllEntities(), this::getFullChunk, new LocalMobCapCalculator(this.chunkMap));
            this.lastSpawnState = state;
            profiler.popPush("filteringLoadedChunks");
            BiArrayList<LevelChunk, ChunkHolder> list = BILIST.get();
            list.clear();
            for (ChunkHolder chunkholder : this.chunkMap.getChunks()) {
                LevelChunk levelchunk = chunkholder.getTickingChunk();
                if (levelchunk != null) {
                    list.add(levelchunk, chunkholder);
                }
            }
            profiler.popPush("spawnAndTick");
            boolean doMobSpawn = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
            list.shuffle(this.randomForTicking);
            for (int i = 0, len = list.size(); i < len; i++) {
                LevelChunk chunk = list.getLeft(i);
                ChunkPos chunkpos = chunk.getPos();
                if (this.level.isNaturalSpawningAllowed(chunkpos)) {
                    chunk.incrementInhabitedTime(deltaTime);
                    if (doMobSpawn &&
                        (this.spawnEnemies || this.spawnFriendlies) &&
                        this.level.getWorldBorder().isWithinBounds(chunkpos) &&
                        this.chunkMap.anyPlayerCloseEnoughForSpawning(chunkpos)) {
                        NaturalSpawner.spawnForChunk(this.level, chunk, state, this.spawnFriendlies, this.spawnEnemies, shouldSpawnAnimals);
                    }
                    if (this.level.shouldTickBlocksAt(chunkpos.toLong())) {
                        this.level.tickChunk(chunk, randomTickSpeed);
                    }
                }
            }
            profiler.popPush("customSpawners");
            if (doMobSpawn) {
                this.level.tickCustomSpawners(this.spawnEnemies, this.spawnFriendlies);
            }
            profiler.popPush("broadcast");
            for (int i = 0, len = list.size(); i < len; i++) {
                list.getRight(i).broadcastChanges(list.getLeft(i));
            }
            profiler.pop();
            profiler.pop();
            this.chunkMap.tick();
            list.clear();
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void updateChunkForced(ChunkPos chunkPos, boolean bl) {
        Evolution.deprecatedMethod();
        throw new RuntimeException("Use non-ChunkPos version!");
    }

    @Override
    public void updateChunkForced_(long chunkPos, boolean adding) {
        this.distanceManager.updateChunkForced_(chunkPos, adding);
    }
}
