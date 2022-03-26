package tgw.evolution.mixin;

import net.minecraft.server.level.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.BiArrayList;
import tgw.evolution.util.math.MathHelper;

import javax.annotation.Nullable;
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
    protected abstract void getFullChunk(long p_8371_, Consumer<LevelChunk> p_8372_);

    /**
     * @author MGSchultz
     * <p>
     * Avoid allocations
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
                if (this.level.isPositionEntityTicking(chunkpos) && this.chunkMap.anyPlayerCloseEnoughForSpawning(chunkpos) ||
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
