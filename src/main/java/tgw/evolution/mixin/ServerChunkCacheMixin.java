package tgw.evolution.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.Util;
import net.minecraft.server.level.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.collection.BiArrayList;
import tgw.evolution.util.math.MathHelper;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin extends ChunkSource {

    private static final ThreadLocal<BiArrayList<LevelChunk, ChunkHolder>> BILIST = ThreadLocal.withInitial(BiArrayList::new);
    @Shadow
    @Final
    public ChunkMap chunkMap;
    @Shadow
    @Final
    public ServerLevel level;
    @Shadow
    @Final
    private DistanceManager distanceManager;
    @Shadow
    private long lastInhabitedUpdate;
    @Shadow
    @Nullable
    private NaturalSpawner.SpawnState lastSpawnState;
    @Shadow
    private boolean spawnEnemies;
    @Shadow
    private boolean spawnFriendlies;

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
        int j = 33 + ChunkStatus.getDistance(status);
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent(longPos);
        if (p_8460_) {
            this.distanceManager.addTicket(longPos, new Ticket<>(TicketType.UNKNOWN, j, new ChunkPos(x, z), false));
            if (this.chunkAbsent(chunkholder, j)) {
                ProfilerFiller profilerfiller = this.level.getProfiler();
                profilerfiller.push("chunkLoad");
                this.runDistanceManagerUpdates();
                chunkholder = this.getVisibleChunkIfPresent(longPos);
                profilerfiller.pop();
                if (this.chunkAbsent(chunkholder, j)) {
                    throw Util.pauseInIde(new IllegalStateException("No chunk holder after ticket has been added"));
                }
            }
        }
        //noinspection ConstantConditions
        return this.chunkAbsent(chunkholder, j) ? ChunkHolder.UNLOADED_CHUNK_FUTURE : chunkholder.getOrScheduleFuture(status, this.chunkMap);
    }

    @Shadow
    protected abstract void getFullChunk(long p_8371_, Consumer<LevelChunk> p_8372_);

    @Shadow
    @Nullable
    protected abstract ChunkHolder getVisibleChunkIfPresent(long p_8365_);

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

    @Shadow
    abstract boolean runDistanceManagerUpdates();

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    private void tickChunks() {
        long gameTime = this.level.getGameTime();
        long deltaTime = gameTime - this.lastInhabitedUpdate;
        this.lastInhabitedUpdate = gameTime;
        boolean flag = this.level.isDebug();
        if (flag) {
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
            list.shuffle(MathHelper.RANDOM);
            for (int i = 0, len = list.size(); i < len; i++) {
                LevelChunk chunk = list.getLeft(i);
                ChunkPos chunkpos = chunk.getPos();
                if (this.level.isNaturalSpawningAllowed(chunkpos) && this.chunkMap.anyPlayerCloseEnoughForSpawning(chunkpos) ||
                    this.distanceManager.shouldForceTicks(chunkpos.toLong())) {
                    chunk.incrementInhabitedTime(deltaTime);
                    if (doMobSpawn && (this.spawnEnemies || this.spawnFriendlies) && this.level.getWorldBorder().isWithinBounds(chunkpos)) {
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
