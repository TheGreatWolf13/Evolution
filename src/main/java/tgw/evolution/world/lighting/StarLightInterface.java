package tgw.evolution.world.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.client.renderer.ambient.DynamicLights;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.patches.PatchTicketType;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.collection.sets.LSet;

import java.util.ArrayDeque;
import java.util.concurrent.CompletableFuture;

public final class StarLightInterface {

    public static final TicketType<ChunkPos> CHUNK_WORK_TICKET = PatchTicketType.create("starlight_chunk_work_ticket", Long::compare, 0);
    public final LevelLightEngine lightEngine;
    private final LayerLightEventListener blockReader;
    private final @Nullable ArrayDeque<BlockStarLightEngine> cachedBlockPropagators;
    private final @Nullable ArrayDeque<SkyStarLightEngine> cachedSkyPropagators;
    private final boolean hasBlockLight;
    private final boolean hasSkyLight;
    private final boolean isClientSide;
    /**
     * Can be {@code null}, indicating the light is all empty.
     */
    private final @Nullable Level level;
    private final @Nullable LightChunkGetter lightAccess;
    private final LightQueue lightQueue = new LightQueue(this);
    private final int maxLightSection;
    private final int maxSection;
    private final int minLightSection;
    private final int minSection;
    private final LayerLightEventListener skyReader;

    public StarLightInterface(@Nullable LightChunkGetter lightAccess, boolean hasSkyLight, boolean hasBlockLight, LevelLightEngine lightEngine) {
        this.lightAccess = lightAccess;
        this.level = lightAccess == null ? null : (Level) lightAccess.getLevel();
        this.cachedSkyPropagators = hasSkyLight && lightAccess != null ? new ArrayDeque<>() : null;
        this.cachedBlockPropagators = hasBlockLight && lightAccess != null ? new ArrayDeque<>() : null;
        this.isClientSide = !(this.level instanceof ServerLevel);
        if (this.level == null) {
            this.minSection = -4;
            this.maxSection = 19;
            this.minLightSection = -5;
            this.maxLightSection = 20;
        }
        else {
            this.minSection = WorldUtil.getMinSection(this.level);
            this.maxSection = WorldUtil.getMaxSection(this.level);
            this.minLightSection = WorldUtil.getMinLightSection(this.level);
            this.maxLightSection = WorldUtil.getMaxLightSection(this.level);
        }
        this.lightEngine = lightEngine;
        this.hasBlockLight = hasBlockLight;
        this.hasSkyLight = hasSkyLight;
        this.skyReader = !hasSkyLight ? LayerLightEventListener.DummyLightLayerEventListener.INSTANCE : new LayerLightEventListener() {

            @Override
            public void checkBlock(BlockPos pos) {
                Evolution.deprecatedMethod();
                this.checkBlock_(pos.asLong());
            }

            @Override
            public void checkBlock_(long pos) {
                StarLightInterface.this.lightEngine.checkBlock_(pos);
            }

            @Override
            public void enableLightSources(ChunkPos pos, boolean bl) {
                Evolution.deprecatedMethod();
                this.enableLightSources_(pos.x, pos.z, bl);
            }

            @Override
            public void enableLightSources_(int secX, int secZ, boolean bl) {
                throw new UnsupportedOperationException();
            }

            @Override
            public @Nullable DataLayer getDataLayerData(SectionPos pos) {
                Evolution.deprecatedMethod();
                return null;
            }

            @Override
            public byte @Nullable [] getDataLayerData_(int secX, int secY, int secZ) {
                ChunkAccess chunk = StarLightInterface.this.getAnyChunkNow(secX, secZ);
                if (chunk == null || !StarLightInterface.this.isClientSide && !chunk.isLightCorrect() || !chunk.getStatus().isOrAfter(ChunkStatus.LIGHT)) {
                    return null;
                }
                if (secY > StarLightInterface.this.maxLightSection || secY < StarLightInterface.this.minLightSection) {
                    return null;
                }
                if (chunk.getSkyEmptinessMap() == null) {
                    return null;
                }
                return chunk.getSkyNibbles()[secY - StarLightInterface.this.minLightSection].toVanillaNibble();
            }

            @Override
            public int getLightValue(BlockPos pos) {
                Evolution.deprecatedMethod();
                return this.getLightValue_(pos.asLong());
            }

            @Override
            public int getLightValue_(long pos) {
                int x = BlockPos.getX(pos);
                int y = BlockPos.getY(pos);
                int z = BlockPos.getZ(pos);
                return StarLightInterface.this.getSkyLightValue(x, y, z, StarLightInterface.this.getAnyChunkNow(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)));
            }

            @Override
            public boolean hasLightWork() {
                // not really correct...
                return StarLightInterface.this.hasUpdates();
            }

            @Override
            public void onBlockEmissionIncrease(BlockPos pos, int lightEmission) {
                Evolution.deprecatedMethod();
                this.onBlockEmissionIncrease_(pos.asLong(), lightEmission);
            }

            @Override
            public void onBlockEmissionIncrease_(long pos, int lightEmission) {
                // skylight doesn't care
            }

            @Override
            public int runUpdates(int i, boolean bl, boolean bl2) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void updateSectionStatus(SectionPos pos, boolean notReady) {
                Evolution.deprecatedMethod();
                this.updateSectionStatus_sec(pos.x(), pos.y(), pos.z(), notReady);
            }

            @Override
            public void updateSectionStatus_sec(int secX, int secY, int secZ, boolean notReady) {
                StarLightInterface.this.sectionChange(secX, secY, secZ, notReady);
            }
        };
        this.blockReader = !hasBlockLight ? LayerLightEventListener.DummyLightLayerEventListener.INSTANCE : new LayerLightEventListener() {

            @Override
            public void checkBlock(BlockPos pos) {
                Evolution.deprecatedMethod();
                this.checkBlock_(pos.asLong());
            }

            @Override
            public void checkBlock_(long pos) {
                StarLightInterface.this.lightEngine.checkBlock_(pos);
            }

            @Override
            public void enableLightSources(ChunkPos chunkPos, boolean bl) {
                Evolution.deprecatedMethod();
                throw new UnsupportedOperationException();
            }

            @Override
            public void enableLightSources_(int secX, int secZ, boolean bl) {
                throw new UnsupportedOperationException();
            }

            @Override
            public @Nullable DataLayer getDataLayerData(SectionPos pos) {
                Evolution.deprecatedMethod();
                return null;
            }

            @Override
            public byte @Nullable [] getDataLayerData_(int secX, int secY, int secZ) {
                ChunkAccess chunk = StarLightInterface.this.getAnyChunkNow(secX, secZ);
                if (chunk == null || secY < StarLightInterface.this.minLightSection || secY > StarLightInterface.this.maxLightSection) {
                    return null;
                }
                return chunk.getBlockShorts()[secY - StarLightInterface.this.minLightSection].toVanillaShort();
            }

            @Override
            public int getLightValue(BlockPos pos) {
                Evolution.deprecatedMethod();
                return this.getLightValue_(pos.asLong());
            }

            @Override
            public int getLightValue_(long pos) {
                int dl = 0;
                if (StarLightInterface.this.level != null && StarLightInterface.this.level.isClientSide) {
                    ClientEvents instance = ClientEvents.getInstance();
                    if (instance.isInitialized()) {
                        dl = instance.getDynamicLights().getLight(pos);
                        if (dl == 0b1_1111_1_1111_1_1111) {
                            return 15;
                        }
                    }
                }
                int x = BlockPos.getX(pos);
                int y = BlockPos.getY(pos);
                int z = BlockPos.getZ(pos);
                int bl = StarLightInterface.this.getBlockLightValue(x, y, z, StarLightInterface.this.getAnyChunkNow(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)));
                if (dl == 0) {
                    return bl;
                }
                return DynamicLights.combine(bl, dl);
            }

            @Override
            public boolean hasLightWork() {
                // not really correct...
                return StarLightInterface.this.hasUpdates();
            }

            @Override
            public void onBlockEmissionIncrease(BlockPos pos, int lightEmission) {
                Evolution.deprecatedMethod();
                this.onBlockEmissionIncrease_(pos.asLong(), lightEmission);
            }

            @Override
            public void onBlockEmissionIncrease_(long pos, int lightEmission) {
                this.checkBlock_(pos);
            }

            @Override
            public int runUpdates(int i, boolean bl, boolean bl2) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void updateSectionStatus(SectionPos pos, boolean notReady) {
                Evolution.deprecatedMethod();
                this.updateSectionStatus_sec(pos.x(), pos.y(), pos.z(), notReady);
            }

            @Override
            public void updateSectionStatus_sec(int secX, int secY, int secZ, boolean notReady) {
                StarLightInterface.this.sectionChange(secX, secY, secZ, notReady);
            }
        };
    }

    public @Nullable CompletableFuture<Void> blockChange(long pos) {
        if (this.level == null) {
            //Empty world
            return null;
        }
        int y = BlockPos.getY(pos);
        if (y < WorldUtil.getMinBlockY(this.level) || y > WorldUtil.getMaxBlockY(this.level)) {
            //Empty world
            return null;
        }
        return this.lightQueue.queueBlockChange(pos);
    }

    public void checkBlockEdges(int chunkX, int chunkZ) {
        BlockStarLightEngine blockEngine = this.getBlockLightEngine();
        try {
            if (blockEngine != null) {
                assert this.lightAccess != null;
                blockEngine.checkChunkEdges(this.lightAccess, chunkX, chunkZ);
            }
        }
        finally {
            if (blockEngine != null) {
                this.releaseBlockLightEngine(blockEngine);
            }
        }
    }

    public void checkChunkEdges(int chunkX, int chunkZ) {
        this.checkSkyEdges(chunkX, chunkZ);
        this.checkBlockEdges(chunkX, chunkZ);
    }

    public void checkSkyEdges(int chunkX, int chunkZ) {
        SkyStarLightEngine skyEngine = this.getSkyLightEngine();
        try {
            if (skyEngine != null) {
                assert this.lightAccess != null;
                skyEngine.checkChunkEdges(this.lightAccess, chunkX, chunkZ);
            }
        }
        finally {
            if (skyEngine != null) {
                this.releaseSkyLightEngine(skyEngine);
            }
        }
    }

    public void forceLoadInChunk(ChunkAccess chunk, Boolean[] emptySections) {
        SkyStarLightEngine skyEngine = this.getSkyLightEngine();
        BlockStarLightEngine blockEngine = this.getBlockLightEngine();
        assert this.lightAccess != null;
        try {
            if (skyEngine != null) {
                skyEngine.forceHandleEmptySectionChanges(this.lightAccess, chunk, emptySections);
            }
            if (blockEngine != null) {
                blockEngine.forceHandleEmptySectionChanges(this.lightAccess, chunk, emptySections);
            }
        }
        finally {
            assert skyEngine != null;
            assert blockEngine != null;
            this.releaseSkyLightEngine(skyEngine);
            this.releaseBlockLightEngine(blockEngine);
        }
    }

    public @Nullable ChunkAccess getAnyChunkNow(int chunkX, int chunkZ) {
        if (this.level == null) {
            // empty world
            return null;
        }
        return this.level.getAnyChunkImmediately(chunkX, chunkZ);
    }

    private @Nullable BlockStarLightEngine getBlockLightEngine() {
        if (this.cachedBlockPropagators == null) {
            return null;
        }
        BlockStarLightEngine ret;
        synchronized (this.cachedBlockPropagators) {
            ret = this.cachedBlockPropagators.pollFirst();
        }
        if (ret == null) {
            assert this.level != null;
            return new BlockStarLightEngine(this.level);
        }
        return ret;
    }

    private int getBlockLightValue(int x, int y, int z, @Nullable ChunkAccess chunk) {
        if (!this.hasBlockLight) {
            return 0;
        }
        int secY = SectionPos.blockToSectionCoord(y);
        int minLightSection = this.minLightSection;
        int maxLightSection = this.maxLightSection;
        if (secY < minLightSection || secY > maxLightSection) {
            return 0;
        }
        if (chunk == null) {
            return 0;
        }
        SWMRShortArray nibble = chunk.getBlockShorts()[secY - minLightSection];
        return nibble.getVisible(x, y, z);
    }

    public LayerLightEventListener getBlockReader() {
        return this.blockReader;
    }

    public int getClampedBlockLight(long pos) {
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        ChunkAccess chunk = this.getAnyChunkNow(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        int value = this.getBlockLightValue(x, y, z, chunk);
        int r = value & 0xF;
        int g = value >>> 5 & 0xF;
        int b = value >>> 10 & 0xF;
        return Math.max(r, Math.max(g, b));
    }

    private int getClampedBlockLightValue(int x, int y, int z, @Nullable ChunkAccess chunk) {
        int value = this.getBlockLightValue(x, y, z, chunk);
        int r = value & 0xF;
        int g = value >>> 5 & 0xF;
        int b = value >>> 10 & 0xF;
        return Math.max(r, Math.max(g, b));
    }

    public @Nullable Level getLevel() {
        return this.level;
    }

    public @Nullable LightChunkGetter getLightAccess() {
        return this.lightAccess;
    }

    public int getRawBrightness(long pos, int ambientDarkness) {
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        ChunkAccess chunk = this.getAnyChunkNow(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        int sky = this.getSkyLightValue(x, y, z, chunk) - ambientDarkness;
        // Don't fetch the block light level if the skylight level is 15, since the value will never be higher.
        if (sky == 15) {
            return 15;
        }
        int block = this.getClampedBlockLightValue(x, y, z, chunk);
        return Math.max(sky, block);
    }

    private @Nullable SkyStarLightEngine getSkyLightEngine() {
        if (this.cachedSkyPropagators == null) {
            return null;
        }
        final SkyStarLightEngine ret;
        synchronized (this.cachedSkyPropagators) {
            ret = this.cachedSkyPropagators.pollFirst();
        }
        if (ret == null) {
            assert this.level != null;
            return new SkyStarLightEngine(this.level);
        }
        return ret;
    }

    private int getSkyLightValue(int x, int y, int z, @Nullable ChunkAccess chunk) {
        if (!this.hasSkyLight) {
            return 0;
        }
        int minSection = this.minSection;
        int maxSection = this.maxSection;
        int minLightSection = this.minLightSection;
        int maxLightSection = this.maxLightSection;
        if (chunk == null || !this.isClientSide && !chunk.isLightCorrect() || !chunk.getStatus().isOrAfter(ChunkStatus.LIGHT)) {
            return 15;
        }
        int sectionY = y >> 4;
        if (sectionY > maxLightSection) {
            return 15;
        }
        if (sectionY < minLightSection) {
            sectionY = minLightSection;
            y = sectionY << 4;
        }
        SWMRNibbleArray[] nibbles = chunk.getSkyNibbles();
        SWMRNibbleArray immediate = nibbles[sectionY - minLightSection];
        if (!immediate.isNullNibbleVisible()) {
            return immediate.getVisible(x, y, z);
        }
        boolean[] emptinessMap = chunk.getSkyEmptinessMap();
        if (emptinessMap == null) {
            return 15;
        }
        // are we above this chunk's lowest empty section?
        int lowestY = minLightSection - 1;
        for (int currY = maxSection; currY >= minSection; --currY) {
            if (emptinessMap[currY - minSection]) {
                continue;
            }
            // should always be full lit here
            lowestY = currY;
            break;
        }
        if (sectionY > lowestY) {
            return 15;
        }
        // this nibble is going to depend solely on the skylight data above it
        // find first non-null data above (there does exist one, as we just found it above)
        for (int currY = sectionY + 1; currY <= maxLightSection; ++currY) {
            SWMRNibbleArray nibble = nibbles[currY - minLightSection];
            if (!nibble.isNullNibbleVisible()) {
                return nibble.getVisible(x, 0, z);
            }
        }
        // should never reach here
        return 15;
    }

    public LayerLightEventListener getSkyReader() {
        return this.skyReader;
    }

    public boolean hasUpdates() {
        return !this.lightQueue.isEmpty();
    }

    public boolean isClientSide() {
        return this.isClientSide;
    }

    public void lightChunk(ChunkAccess chunk, Boolean[] emptySections) {
        final SkyStarLightEngine skyEngine = this.getSkyLightEngine();
        final BlockStarLightEngine blockEngine = this.getBlockLightEngine();
        try {
            assert this.lightAccess != null;
            if (skyEngine != null) {
                skyEngine.light(this.lightAccess, chunk, emptySections);
            }
            if (blockEngine != null) {
                blockEngine.light(this.lightAccess, chunk, emptySections);
            }
        }
        finally {
            if (skyEngine != null) {
                this.releaseSkyLightEngine(skyEngine);
            }
            if (blockEngine != null) {
                this.releaseBlockLightEngine(blockEngine);
            }
        }
    }

    public void propagateChanges() {
        if (this.lightQueue.isEmpty()) {
            return;
        }
        SkyStarLightEngine skyEngine = this.getSkyLightEngine();
        BlockStarLightEngine blockEngine = this.getBlockLightEngine();
        assert this.lightAccess != null;
        try {
            LightQueue.ChunkTasks task;
            while ((task = this.lightQueue.removeFirstTask()) != null) {
                if (task.lightTasks != null) {
                    for (Runnable run : task.lightTasks) {
                        run.run();
                    }
                }
                long coordinate = task.chunkCoordinate;
                int chunkX = ChunkPos.getX(coordinate);
                int chunkZ = ChunkPos.getZ(coordinate);
                LSet positions = task.changedPositions;
                Boolean[] sectionChanges = task.changedSectionSet;
                if (skyEngine != null && (!positions.isEmpty() || sectionChanges != null)) {
                    skyEngine.blocksChangedInChunk(this.lightAccess, chunkX, chunkZ, positions, sectionChanges);
                }
                if (blockEngine != null && (!positions.isEmpty() || sectionChanges != null)) {
                    blockEngine.blocksChangedInChunk(this.lightAccess, chunkX, chunkZ, positions, sectionChanges);
                }
                if (skyEngine != null && task.queuedEdgeChecksSky != null) {
                    skyEngine.checkChunkEdges(this.lightAccess, chunkX, chunkZ, task.queuedEdgeChecksSky);
                }
                if (blockEngine != null && task.queuedEdgeChecksBlock != null) {
                    blockEngine.checkChunkEdges(this.lightAccess, chunkX, chunkZ, task.queuedEdgeChecksBlock);
                }
                task.onComplete.complete(null);
            }
        }
        finally {
            if (skyEngine != null) {
                this.releaseSkyLightEngine(skyEngine);
            }
            if (blockEngine != null) {
                this.releaseBlockLightEngine(blockEngine);
            }
        }
    }

    private void releaseBlockLightEngine(BlockStarLightEngine engine) {
        if (this.cachedBlockPropagators == null) {
            return;
        }
        synchronized (this.cachedBlockPropagators) {
            this.cachedBlockPropagators.addFirst(engine);
        }
    }

    private void releaseSkyLightEngine(SkyStarLightEngine engine) {
        if (this.cachedSkyPropagators == null) {
            return;
        }
        synchronized (this.cachedSkyPropagators) {
            this.cachedSkyPropagators.addFirst(engine);
        }
    }

    public void scheduleChunkLight(ChunkPos pos, Runnable run) {
        this.lightQueue.queueChunkLighting(pos, run);
    }

    public @Nullable CompletableFuture<Void> sectionChange(int secX, int secY, int secZ, boolean newEmptyValue) {
        if (this.level == null) { // empty world
            return null;
        }
        return this.lightQueue.queueSectionChange(secX, secY, secZ, newEmptyValue);
    }

    protected static final class LightQueue {

        private final Long2ObjectLinkedOpenHashMap<ChunkTasks> chunkTasks = new Long2ObjectLinkedOpenHashMap<>();
        private final StarLightInterface manager;

        public LightQueue(final StarLightInterface manager) {
            this.manager = manager;
        }

        public synchronized boolean isEmpty() {
            return this.chunkTasks.isEmpty();
        }

        public synchronized CompletableFuture<Void> queueBlockChange(long pos) {
            int x = BlockPos.getX(pos);
            int z = BlockPos.getZ(pos);
            long key = ChunkPos.asLong(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
            ChunkTasks tasks = this.chunkTasks.get(key);
            if (tasks == null) {
                tasks = new ChunkTasks(key);
                this.chunkTasks.put(key, tasks);
            }
            tasks.changedPositions.add(pos);
            return tasks.onComplete;
        }

        public synchronized void queueChunkLighting(ChunkPos pos, Runnable lightTask) {
            long key = pos.toLong();
            ChunkTasks tasks = this.chunkTasks.get(key);
            if (tasks == null) {
                tasks = new ChunkTasks(key);
                this.chunkTasks.put(key, tasks);
            }
            if (tasks.lightTasks == null) {
                tasks.lightTasks = new OArrayList<>();
            }
            tasks.lightTasks.add(lightTask);
        }

        public synchronized CompletableFuture<Void> queueSectionChange(int secX, int secY, int secZ, boolean newEmptyValue) {
            long key = ChunkPos.asLong(secX, secZ);
            ChunkTasks tasks = this.chunkTasks.get(key);
            if (tasks == null) {
                tasks = new ChunkTasks(key);
                this.chunkTasks.put(key, tasks);
            }
            if (tasks.changedSectionSet == null) {
                tasks.changedSectionSet = new Boolean[this.manager.maxSection - this.manager.minSection + 1];
            }
            tasks.changedSectionSet[secY - this.manager.minSection] = newEmptyValue;
            return tasks.onComplete;
        }

        public synchronized @Nullable ChunkTasks removeFirstTask() {
            if (this.chunkTasks.isEmpty()) {
                return null;
            }
            return this.chunkTasks.removeFirst();
        }

        protected static final class ChunkTasks {

            public final LSet changedPositions = new LHashSet();
            public final long chunkCoordinate;
            public final CompletableFuture<Void> onComplete = new CompletableFuture<>();
            public Boolean @Nullable [] changedSectionSet;
            public @Nullable OList<Runnable> lightTasks;
            public @Nullable ShortOpenHashSet queuedEdgeChecksBlock;
            public @Nullable ShortOpenHashSet queuedEdgeChecksSky;

            public ChunkTasks(long chunkCoordinate) {
                this.chunkCoordinate = chunkCoordinate;
            }
        }
    }
}