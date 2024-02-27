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
import tgw.evolution.patches.PatchServerChunkCache;
import tgw.evolution.util.collection.lists.BiArrayList;
import tgw.evolution.util.math.FastRandom;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.random.RandomGenerator;

@Mixin(ServerChunkCache.class)
public abstract class MixinServerChunkCache extends ChunkSource implements PatchServerChunkCache {

    @Unique private static final ThreadLocal<BiArrayList<LevelChunk, ChunkHolder>> BILIST = ThreadLocal.withInitial(BiArrayList::new);
    @Unique private final RandomGenerator randomForTicking = new FastRandom();
    @Shadow @Final public ChunkMap chunkMap;
    @Shadow @Final ServerLevel level;
    @Shadow @Final private DistanceManager distanceManager;
    @Shadow private long lastInhabitedUpdate;
    @Shadow private @Nullable NaturalSpawner.SpawnState lastSpawnState;
    @Shadow @Final private ServerChunkCache.MainThreadExecutor mainThreadProcessor;
    @Shadow private boolean spawnEnemies;
    @Shadow private boolean spawnFriendlies;

    @Overwrite
    public void blockChanged(BlockPos pos) {
        Evolution.deprecatedMethod();
        this.blockChanged_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void blockChanged_(int x, int y, int z) {
        int secX = SectionPos.blockToSectionCoord(x);
        int secZ = SectionPos.blockToSectionCoord(z);
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(ChunkPos.asLong(secX, secZ));
        if (chunkHolder != null) {
            chunkHolder.blockChanged_(x, y, z);
        }
    }

    @Shadow
    protected abstract boolean chunkAbsent(@Nullable ChunkHolder p_8417_, int p_8418_);

    /**
     * @author TheGreatWolf
     * @reason Delay ChunkPos allocation
     */
    @Overwrite
    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFutureMainThread(int x,
                                                                                                             int z,
                                                                                                             ChunkStatus status,
                                                                                                             boolean p_8460_) {
        long longPos = ChunkPos.asLong(x, z);
        int ticketLevel = 33 + ChunkStatus.getDistance(status);
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent(longPos);
        if (p_8460_) {
            ChunkPos chunkPos = new ChunkPos(x, z);
            this.distanceManager.addTicket(TicketType.UNKNOWN, chunkPos, ticketLevel, chunkPos);
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
        return this.chunkAbsent(chunkholder, ticketLevel) ?
               ChunkHolder.UNLOADED_CHUNK_FUTURE :
               chunkholder.getOrScheduleFuture(status, this.chunkMap);
    }

    @Shadow
    protected abstract void getFullChunk(long p_8371_, Consumer<LevelChunk> p_8372_);

    @Shadow
    protected abstract @Nullable ChunkHolder getVisibleChunkIfPresent(long p_8365_);

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Override
    @Overwrite
    public boolean hasChunk(int pX, int pZ) {
        ChunkHolder holder = this.getVisibleChunkIfPresent(ChunkPos.asLong(pX, pZ));
        int ticket = 33 + ChunkStatus.getDistance(ChunkStatus.FULL);
        return !this.chunkAbsent(holder, ticket);
    }

    @Override
    @Overwrite
    public void onLightUpdate(LightLayer lightLayer, SectionPos pos) {
        Evolution.deprecatedMethod();
        this.onLightUpdate_(lightLayer, pos.x(), pos.y(), pos.z());
    }

    @Override
    public void onLightUpdate_(LightLayer lightLayer, int secX, int secY, int secZ) {
        this.mainThreadProcessor.execute(() -> {
            ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(ChunkPos.asLong(secX, secZ));
            if (chunkHolder != null) {
                chunkHolder.sectionLightChanged(lightLayer, secY);
            }
        });
    }

    @Shadow
    abstract boolean runDistanceManagerUpdates();

    @Shadow
    public abstract void setSimulationDistance(int i);

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
            NaturalSpawner.SpawnState state = NaturalSpawner.createState(chunkCount, this.level.getAllEntities(), this::getFullChunk,
                                                                         new LocalMobCapCalculator(this.chunkMap));
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
}
