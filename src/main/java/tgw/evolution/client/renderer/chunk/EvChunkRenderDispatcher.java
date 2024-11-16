package tgw.evolution.client.renderer.chunk;

import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.sets.RHashSet;
import tgw.evolution.util.collection.sets.RSet;
import tgw.evolution.util.constants.RenderLayer;
import tgw.evolution.util.math.FastRandom;
import tgw.evolution.util.math.IRandom;
import tgw.evolution.util.physics.EarthHelper;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class EvChunkRenderDispatcher {

    private float camX;
    private float camY;
    private float camZ;
    private final Executor executor;
    private final ChunkBuilderPack fixedBuffers;
    private volatile int freeBufferCount;
    private final Queue<ChunkBuilderPack> freeBuffers;
    private final EvVisGraph graph = new EvVisGraph();
    private int highPriorityQuota = 2;
    private ClientLevel level;
    private final ProcessorMailbox<Runnable> mailbox;
    private final PoseStack matrices = new PoseStack();
    private final IRandom random = new FastRandom();
    private final EvLevelRenderer renderer;
    private volatile int toBatchCount;
    private final PriorityBlockingQueue<RenderChunk.ChunkCompileTask> toBatchHighPriority = new PriorityBlockingQueue<>();
    private final Queue<RenderChunk.ChunkCompileTask> toBatchLowPriority = new LinkedBlockingDeque<>();
    private final Queue<Runnable> toUpload = new ConcurrentLinkedQueue<>();

    public EvChunkRenderDispatcher(ClientLevel level, EvLevelRenderer renderer, Executor executor, boolean is64Bit, ChunkBuilderPack builderPack) {
        this(level, renderer, executor, is64Bit, builderPack, -1);
    }

    public EvChunkRenderDispatcher(ClientLevel level, EvLevelRenderer renderer, Executor executor, boolean is64Bit, ChunkBuilderPack builderPack, int countRenderBuilders) {
        this.level = level;
        this.renderer = renderer;
        int sizeNeeded = 0;
        for (RenderType renderType : ChunkBuilderPack.RENDER_TYPES) {
            sizeNeeded += renderType.bufferSize();
        }
        int mem = Math.max(1, (int) (Runtime.getRuntime().maxMemory() * 0.3) / (sizeNeeded * 4) - 1);
        int processors = Runtime.getRuntime().availableProcessors();
        int cappedProcessors = is64Bit ? processors : Math.min(processors, 4);
        int numBuf = countRenderBuilders < 0 ? Math.max(1, Math.min(cappedProcessors, mem)) : countRenderBuilders;
        this.fixedBuffers = builderPack;
        OList<ChunkBuilderPack> list = new OArrayList<>(numBuf);
        try {
            for (int i = 0; i < numBuf; ++i) {
                //noinspection ObjectAllocationInLoop
                list.add(new ChunkBuilderPack());
            }
        }
        catch (OutOfMemoryError e) {
            Evolution.warn("Allocated only {}/{} buffers", list.size(), numBuf);
            int toRemove = Math.min(list.size() * 2 / 3, list.size() - 1);
            for (int i = 0; i < toRemove; ++i) {
                list.remove(list.size() - 1);
            }
            System.gc();
        }
        this.freeBuffers = new ArrayDeque<>(list);
        this.freeBufferCount = this.freeBuffers.size();
        this.executor = executor;
        this.mailbox = ProcessorMailbox.create(executor, "Chunk Renderer");
        this.mailbox.tell(this::runTask);
    }

    @Contract(pure = true)
    public static long chunkPos(int x, int z) {
        return ChunkPos.asLong(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
    }

    private static CompletableFuture<Void> doUploadChunkLayer(BufferBuilder builder, VertexBuffer buffer) {
        return buffer.uploadLater(builder);
    }

    public void blockUntilClear() {
        this.clearBatchQueue();
    }

    private void clearBatchQueue() {
        while (!this.toBatchHighPriority.isEmpty()) {
            RenderChunk.ChunkCompileTask compileTask = this.toBatchHighPriority.poll();
            if (compileTask != null) {
                compileTask.cancel();
            }
        }
        while (!this.toBatchLowPriority.isEmpty()) {
            RenderChunk.ChunkCompileTask compileTask = this.toBatchLowPriority.poll();
            if (compileTask != null) {
                compileTask.cancel();
            }
        }
        this.toBatchCount = 0;
    }

    public void dispose() {
        this.clearBatchQueue();
        this.mailbox.close();
        this.freeBuffers.clear();
    }

    public int getFreeBufferCount() {
        return this.freeBufferCount;
    }

    public String getStats() {
        return String.format("pC: %03d, pU: %02d, aB: %02d", this.toBatchCount, this.toUpload.size(), this.freeBufferCount);
    }

    public int getToBatchCount() {
        return this.toBatchCount;
    }

    public int getToUpload() {
        return this.toUpload.size();
    }

    public boolean isQueueEmpty() {
        return this.toBatchCount == 0 && this.toUpload.isEmpty();
    }

    private @Nullable RenderChunk.ChunkCompileTask pollTask() {
        if (this.highPriorityQuota <= 0) {
            RenderChunk.ChunkCompileTask compileTask = this.toBatchLowPriority.poll();
            if (compileTask != null) {
                this.highPriorityQuota = 2;
                return compileTask;
            }
        }
        RenderChunk.ChunkCompileTask compileTask = this.toBatchHighPriority.poll();
        if (compileTask != null) {
            --this.highPriorityQuota;
            return compileTask;
        }
        this.highPriorityQuota = 2;
        return this.toBatchLowPriority.poll();
    }

    public void rebuildChunkSync(RenderChunk chunk) {
        chunk.compileSync();
    }

    private void runTask() {
        if (!this.freeBuffers.isEmpty()) {
            RenderChunk.ChunkCompileTask compileTask = this.pollTask();
            if (compileTask != null) {
                ChunkBuilderPack builderPack = this.freeBuffers.poll();
                this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
                this.freeBufferCount = this.freeBuffers.size();
                assert builderPack != null;
                CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName(compileTask.name(), () -> compileTask.doTask(builderPack)), this.executor).thenCompose(Function.identity()).whenComplete((task, t) -> {
                    if (t != null) {
                        CrashReport crashreport = CrashReport.forThrowable(t, "Batching chunks");
                        Minecraft.getInstance().delayCrash(() -> Minecraft.getInstance().fillReport(crashreport));
                    }
                    else {
                        this.mailbox.tell(() -> {
                            if (task == ChunkTaskResult.SUCCESSFUL) {
                                builderPack.clearAll();
                            }
                            else {
                                builderPack.discardAll();
                            }
                            this.freeBuffers.add(builderPack);
                            this.freeBufferCount = this.freeBuffers.size();
                            this.runTask();
                        });
                    }
                });
            }
        }
    }

    /**
     * This method only runs on the Main Thread. It schedules work to be done off-thread.
     */
    private void schedule(RenderChunk.ChunkCompileTask compileTask) {
        this.mailbox.tell(() -> {
            if (compileTask.isHighPriority) {
                this.toBatchHighPriority.offer(compileTask);
            }
            else {
                this.toBatchLowPriority.offer(compileTask);
            }
            this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
            this.runTask();
        });
    }

    public void setCamera(float x, float y, float z) {
        this.camX = x;
        this.camY = y;
        this.camZ = z;
    }

    public void setLevel(ClientLevel level) {
        this.level = level;
    }

    public void uploadAllPendingUploads() {
        for (Runnable runnable = this.toUpload.poll(); runnable != null; runnable = this.toUpload.poll()) {
            runnable.run();
        }
    }

    public CompletableFuture<Void> uploadChunkLayer(BufferBuilder builder, VertexBuffer buffer) {
        return CompletableFuture.runAsync(() -> {}, this.toUpload::add).thenCompose(v -> doUploadChunkLayer(builder, buffer));
    }

    public static class CompiledChunk {
        public static final CompiledChunk EMPTY = new CompiledChunk() {
            @Override
            public boolean facesCanSeeEachother(Direction face, Direction other) {
                return true;
            }

            @Override
            public boolean isEmpty(@RenderLayer int renderType) {
                return true;
            }
        };
        public static final CompiledChunk UNCOMPILED = new CompiledChunk() {
            @Override
            public boolean facesCanSeeEachother(Direction face, Direction other) {
                return false;
            }

            @Override
            public boolean isEmpty(@RenderLayer int renderType) {
                return true;
            }
        };
        /**
         * Bits arranged as per {@link RenderLayer}.
         */
        protected byte hasBlocks;
        /**
         * Bits arranged as per {@link RenderLayer}.
         */
        protected byte hasLayer;
        final OList<BlockEntity> renderableTEs = new OArrayList<>();
        public @Nullable BufferBuilder.SortState transparencyState;
        private long visibilitySet;

        public boolean facesCanSeeEachother(Direction face, Direction other) {
            return (this.visibilitySet & 1L << face.ordinal() + other.ordinal() * 6) != 0;
        }

        public List<BlockEntity> getRenderableTEs() {
            return this.renderableTEs;
        }

        public boolean isEmpty(@RenderLayer int renderType) {
            return (this.hasBlocks & 1 << renderType) == 0;
        }
    }

    public class RenderChunk {
        private final VertexBuffer[] buffers = new VertexBuffer[5];
        /**
         * Only access from the Main Thread. <br>
         * Bit 0: hasSolid;<br>
         * Bit 1: hasCutoutMipped;<br>
         * Bit 2: hasCutout;<br>
         * Bit 3: hasTranslucent;<br>
         * Bit 4: hasTripwire;<br>
         * Bit 6: hasRenderableTileEntities;<br>
         * Bit 7: isUncompiled;<br>
         */
        private byte cachedFlags;
        protected volatile CompiledChunk compiled = CompiledChunk.UNCOMPILED;
        /**
         * Only access from the Main Thread.
         */
        private boolean dirty = true;
        private final RSet<BlockEntity> globalBlockEntities = new RHashSet<>();
        public final int index;
        private @Nullable RebuildTask lastRebuildTask;
        private @Nullable ResortTransparencyTask lastResortTransparencyTask;
        /**
         * This value is set from other threads, not atomically.
         */
        private boolean needsUpdate = true;
        /**
         * Only access from the Main Thread.
         */
        private boolean playerChanged;
        /**
         * Only access from the Main Thread.
         */
        public @Visibility int visibility;
        private int x;
        private int y;
        private int z;

        public RenderChunk(int index, int x, int y, int z) {
            for (int i = 0, len = this.buffers.length; i < len; i++) {
                //noinspection ObjectAllocationInLoop,resource
                this.buffers[i] = new VertexBuffer();
            }
            this.index = index;
            this.setOrigin(x, y, z);
        }

        public void beginLayer(BufferBuilder builder) {
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        }

        protected boolean cancelTasks() {
            boolean didCancel = false;
            if (this.lastRebuildTask != null) {
                this.lastRebuildTask.cancel();
                this.lastRebuildTask = null;
                didCancel = true;
            }
            if (this.lastResortTransparencyTask != null) {
                this.lastResortTransparencyTask.cancel();
                this.lastResortTransparencyTask = null;
            }
            return didCancel;
        }

        /**
         * This method only runs on the Main Thread.
         */
        public void compileSync() {
            int chunkX = SectionPos.blockToSectionCoord(this.x);
            int chunkZ = SectionPos.blockToSectionCoord(this.z);
            LevelChunk chunk = EvChunkRenderDispatcher.this.level.getChunk(chunkX, chunkZ);
            if (chunk.isYSpaceEmpty(this.y, this.y + 15)) {
                RenderChunk.this.updateGlobalBlockEntities(RSet.emptySet());
                RenderChunk.this.compiled = CompiledChunk.EMPTY;
                RenderChunk.this.cachedFlags = 0;
                EvChunkRenderDispatcher.this.renderer.addRecentlyCompiledChunk(RenderChunk.this);
                return;
            }
            CompiledChunk oldChunk = this.compiled;
            CompiledChunk compiledChunk;
            if (oldChunk == CompiledChunk.UNCOMPILED || oldChunk == CompiledChunk.EMPTY) {
                compiledChunk = new CompiledChunk();
            }
            else {
                compiledChunk = oldChunk;
                compiledChunk.visibilitySet = 0;
                compiledChunk.hasBlocks = 0;
                compiledChunk.hasLayer = 0;
                compiledChunk.renderableTEs.clear();
                compiledChunk.transparencyState = null;
            }
            ChunkBuilderPack fixedBuffers = EvChunkRenderDispatcher.this.fixedBuffers;
            RSet<BlockEntity> blockEntities = null;
            int posX = this.x;
            int posY = this.y;
            int posZ = this.z;
            EvVisGraph visgraph = EvChunkRenderDispatcher.this.graph;
            visgraph.reset();
            PoseStack matrices = EvChunkRenderDispatcher.this.matrices.reset();
            ModelBlockRenderer.enableCaching();
            IRandom random = EvChunkRenderDispatcher.this.random;
            BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            for (int dx = 0; dx < 16; ++dx) {
                int px = posX + dx;
                for (int dy = 0; dy < 16; ++dy) {
                    int py = posY + dy;
                    for (int dz = 0; dz < 16; ++dz) {
                        int pz = posZ + dz;
                        BlockState blockState = chunk.getBlockState_(px, py, pz);
                        if (blockState.isSolidRender_(EvChunkRenderDispatcher.this.level, px, py, pz)) {
                            visgraph.setOpaque(dx, dy, dz);
                        }
                        if (blockState.hasBlockEntity()) {
                            BlockEntity tile = chunk.getBlockEntity_(px, py, pz);
                            if (tile != null) {
                                BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(tile);
                                if (renderer != null) {
                                    if (renderer.shouldRenderOffScreen(tile)) {
                                        if (blockEntities == null) {
                                            blockEntities = new RHashSet<>();
                                        }
                                        blockEntities.add(tile);
                                    }
                                    else {
                                        compiledChunk.renderableTEs.add(tile);
                                    }
                                }
                            }
                        }
                        FluidState fluidState = chunk.getFluidState_(px, py, pz);
                        for (int i = RenderLayer.SOLID, len = ChunkBuilderPack.RENDER_TYPES.length; i < len; i++) {
                            RenderType renderType = ChunkBuilderPack.RENDER_TYPES[i];
                            if (!fluidState.isEmpty() && ItemBlockRenderTypes.getRenderLayer(fluidState) == renderType) {
                                BufferBuilder builder = fixedBuffers.builder(i);
                                if ((compiledChunk.hasLayer & 1 << i) == 0) {
                                    compiledChunk.hasLayer |= (byte) (1 << i);
                                    RenderChunk.this.beginLayer(builder);
                                }
                                if (dispatcher.renderLiquid(px, py, pz, EvChunkRenderDispatcher.this.level, builder, blockState, fluidState)) {
                                    compiledChunk.hasBlocks |= (byte) (1 << i);
                                }
                            }
                            if (blockState.getRenderShape() != RenderShape.INVISIBLE &&
                                ItemBlockRenderTypes.getChunkRenderType(blockState) == renderType) {
                                BufferBuilder builder = fixedBuffers.builder(i);
                                if ((compiledChunk.hasLayer & 1 << i) == 0) {
                                    compiledChunk.hasLayer |= (byte) (1 << i);
                                    RenderChunk.this.beginLayer(builder);
                                }
                                matrices.pushPose();
                                matrices.translate(dx, dy, dz);
                                if (dispatcher.renderBatched(blockState, px, py, pz, EvChunkRenderDispatcher.this.level, matrices, builder, true, random)) {
                                    compiledChunk.hasBlocks |= (byte) (1 << i);
                                }
                                matrices.popPose();
                            }
                        }
                    }
                }
            }
            compiledChunk.visibilitySet = visgraph.resolve();
            if ((compiledChunk.hasBlocks & 1 << RenderLayer.TRANSLUCENT) != 0) {
                BufferBuilder builder = fixedBuffers.builder(RenderLayer.TRANSLUCENT);
                builder.setQuadSortOrigin(EvChunkRenderDispatcher.this.camX - posX, EvChunkRenderDispatcher.this.camY - posY, EvChunkRenderDispatcher.this.camZ - posZ);
                compiledChunk.transparencyState = builder.getSortState();
            }
            for (int i = RenderLayer.SOLID, len = ChunkBuilderPack.RENDER_TYPES.length; i < len; i++) {
                if ((compiledChunk.hasLayer & 1 << i) != 0) {
                    fixedBuffers.builder(i).end();
                }
            }
            ModelBlockRenderer.clearCache();
            blockEntities = blockEntities == null ? RSet.emptySet() : blockEntities;
            RenderChunk.this.updateGlobalBlockEntities(blockEntities);
            for (int i = RenderLayer.SOLID, len = ChunkBuilderPack.RENDER_TYPES.length; i < len; i++) {
                if ((compiledChunk.hasLayer & 1 << i) != 0) {
                    RenderChunk.this.getBuffer(i).upload(fixedBuffers.builder(i));
                }
            }
            RenderChunk.this.compiled = compiledChunk;
            RenderChunk.this.needsUpdate = true;
            EvChunkRenderDispatcher.this.renderer.addRecentlyCompiledChunk(RenderChunk.this);
        }

        private boolean doesChunkExistAt(int posX, int posZ) {
            return EvChunkRenderDispatcher.this.level.getChunk(SectionPos.blockToSectionCoord(posX), SectionPos.blockToSectionCoord(posZ), ChunkStatus.FULL, false) != null;
        }

        public VertexBuffer getBuffer(@RenderLayer int renderType) {
            return this.buffers[renderType];
        }

        protected double getDistToCameraSqr() {
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vec3 position = camera.getPosition();
            double x = this.x + 8 - position.x;
            double y = this.y + 8 - position.y;
            double z = this.z + 8 - position.z;
            return x * x + y * y + z * z;
        }

        public byte getRenderLayers() {
            if (this.needsUpdate) {
                this.updateCache();
            }
            return (byte) (this.cachedFlags & 0b1_1111);
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getZ() {
            return this.z;
        }

        public boolean hasAllNeighbors() {
            if (this.getDistToCameraSqr() <= 24 * 24) {
                return true;
            }
            return this.doesChunkExistAt(this.x - 16, this.z) &&
                   this.doesChunkExistAt(this.x, this.z - 16) &&
                   this.doesChunkExistAt(this.x + 16, this.z) &&
                   this.doesChunkExistAt(this.x, this.z + 16);
        }

        public boolean hasRenderableTEs() {
            if (this.needsUpdate) {
                this.updateCache();
            }
            return (this.cachedFlags & 1 << 6) != 0;
        }

        /**
         * This method only runs on the Main Thread.
         */
        public boolean isCompletelyEmpty() {
            if (this.needsUpdate) {
                this.updateCache();
            }
            return this.cachedFlags == 0;
        }

        /**
         * This method only runs on the Main Thread.
         */
        public boolean isDirty() {
            return this.dirty;
        }

        /**
         * This method only runs on the Main Thread.
         */
        public boolean isDirtyFromPlayer() {
            return this.dirty && this.playerChanged;
        }

        /**
         * This method only runs on the Main Thread.
         */
        public boolean isEmpty(@RenderLayer int renderType) {
            if (this.needsUpdate) {
                this.updateCache();
            }
            return (this.cachedFlags & 1 << renderType) == 0;
        }

        /**
         * This method only runs on the Main Thread.
         */
        public void rebuildChunkAsync(EvChunkRenderDispatcher dispatcher, EvRenderRegionCache cache) {
            boolean canceled = this.cancelTasks();
            EvRenderChunkRegion region = cache.createRegion(EvChunkRenderDispatcher.this.level, this.x - 1, this.y - 1, this.z - 1, this.x + 16, this.y + 16, this.z + 16, 1);
            if (region == null) {
                RenderChunk.this.updateGlobalBlockEntities(RSet.emptySet());
                RenderChunk.this.cachedFlags = 0;
                RenderChunk.this.compiled = CompiledChunk.EMPTY;
                EvChunkRenderDispatcher.this.renderer.addRecentlyCompiledChunk(RenderChunk.this);
                return;
            }
            this.lastRebuildTask = new RenderChunk.RebuildTask(this.getDistToCameraSqr(), region, canceled || this.compiled != CompiledChunk.UNCOMPILED);
            dispatcher.schedule(this.lastRebuildTask);
        }

        /**
         * This method only runs in the Main Thread.
         */
        public void releaseBuffers() {
            this.reset();
            for (VertexBuffer buffer : this.buffers) {
                buffer.close();
            }
        }

        /**
         * This method only runs on the Main Thread.
         */
        private void reset() {
            this.cancelTasks();
            this.compiled = CompiledChunk.UNCOMPILED;
            this.cachedFlags = (byte) (1 << 7);
            this.dirty = true;
        }

        /**
         * This method only runs on the Main Thread.
         */
        public boolean resortTransparency(EvChunkRenderDispatcher dispatcher, boolean onThread) {
            if (this.lastResortTransparencyTask != null) {
                this.lastResortTransparencyTask.cancel();
            }
            if (this.isEmpty(RenderLayer.TRANSLUCENT)) {
                return false;
            }
            if (onThread) {
                if (!RenderChunk.this.hasAllNeighbors()) {
                    return true;
                }
                CompiledChunk compiled = this.compiled;
                BufferBuilder.SortState sortState = compiled.transparencyState;
                if (sortState != null) {
                    BufferBuilder builder = EvChunkRenderDispatcher.this.fixedBuffers.builder(RenderLayer.TRANSLUCENT);
                    RenderChunk.this.beginLayer(builder);
                    builder.restoreSortState(sortState);
                    builder.setQuadSortOrigin(EvChunkRenderDispatcher.this.camX - RenderChunk.this.x,
                                              EvChunkRenderDispatcher.this.camY - RenderChunk.this.y,
                                              EvChunkRenderDispatcher.this.camZ - RenderChunk.this.z);
                    compiled.transparencyState = builder.getSortState();
                    builder.end();
                    RenderChunk.this.getBuffer(RenderLayer.TRANSLUCENT)
                                    .upload(EvChunkRenderDispatcher.this.fixedBuffers.builder(RenderLayer.TRANSLUCENT));
                }
                return true;
            }
            this.lastResortTransparencyTask = new ResortTransparencyTask(this.getDistToCameraSqr(), this.compiled);
            dispatcher.schedule(this.lastResortTransparencyTask);
            return true;
        }

        /**
         * This method only runs on the Main Thread.
         */
        public void setDirty(boolean reRenderOnMainThread) {
            boolean wasDirty = this.dirty;
            this.dirty = true;
            this.playerChanged = reRenderOnMainThread | (wasDirty && this.playerChanged);
        }

        /**
         * This method only runs on the Main Thread.
         */
        public void setNotDirty() {
            this.dirty = false;
            this.playerChanged = false;
        }

        /**
         * This method only runs on the Main Thread.
         */
        public void setOrigin(int x, int y, int z) {
            if (EarthHelper.wrapBlockCoordinate(this.x) != EarthHelper.wrapBlockCoordinate(x) || this.y != y || EarthHelper.wrapBlockCoordinate(this.z) != EarthHelper.wrapBlockCoordinate(z)) {
                this.reset();
            }
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * This method only runs on the Main Thread.
         */
        private void updateCache() {
            this.needsUpdate = false;
            CompiledChunk compiled = this.compiled;
            this.cachedFlags = compiled.hasBlocks;
            if (compiled == CompiledChunk.UNCOMPILED) {
                this.cachedFlags |= (byte) (1 << 7);
            }
            else if (!compiled.renderableTEs.isEmpty()) {
                this.cachedFlags |= 1 << 6;
            }
        }

        void updateGlobalBlockEntities(RSet<BlockEntity> blockEntities) {
            if (!blockEntities.isEmpty()) {
                RSet<BlockEntity> toAdd = new RHashSet<>(blockEntities);
                RSet<BlockEntity> toRemove;
                synchronized (this.globalBlockEntities) {
                    toRemove = new RHashSet<>(this.globalBlockEntities);
                    toAdd.removeAll(this.globalBlockEntities);
                    toRemove.removeAll(blockEntities);
                    this.globalBlockEntities.clear();
                    this.globalBlockEntities.addAll(blockEntities);
                }
                EvChunkRenderDispatcher.this.renderer.updateGlobalBlockEntities(toRemove, toAdd);
            }
            else {
                EvChunkRenderDispatcher.this.renderer.updateGlobalBlockEntities(this.globalBlockEntities, blockEntities);
            }
        }

        public abstract static class ChunkCompileTask implements Comparable<ChunkCompileTask> {
            protected final double distAtCreation;
            protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
            protected final boolean isHighPriority;

            public ChunkCompileTask(double distAtCreation, boolean isHighPriority) {
                this.distAtCreation = distAtCreation;
                this.isHighPriority = isHighPriority;
            }

            public abstract void cancel();

            @Override
            public int compareTo(ChunkCompileTask other) {
                return Doubles.compare(this.distAtCreation, other.distAtCreation);
            }

            public abstract CompletableFuture<ChunkTaskResult> doTask(ChunkBuilderPack builderPack);

            protected abstract String name();
        }

        class RebuildTask extends ChunkCompileTask {

            static final ThreadLocal<EvVisGraph> GRAPH_CACHE = ThreadLocal.withInitial(EvVisGraph::new);
            static final ThreadLocal<PoseStack> MATRICES_CACHE = ThreadLocal.withInitial(PoseStack::new);
            static final ThreadLocal<IRandom> RANDOM_CACHE = ThreadLocal.withInitial(FastRandom::new);
            protected @Nullable EvRenderChunkRegion region;

            public RebuildTask(double distAtCreation, @Nullable EvRenderChunkRegion region, boolean isHighPriority) {
                super(distAtCreation, isHighPriority);
                this.region = region;
            }

            @Override
            public void cancel() {
                this.region = null;
                if (this.isCancelled.compareAndSet(false, true)) {
                    RenderChunk.this.setDirty(false);
                }
            }

            private RSet<BlockEntity> compile(float camX, float camY, float camZ, CompiledChunk compiledChunk, ChunkBuilderPack builderPack) {
                RSet<BlockEntity> blockEntities = null;
                EvRenderChunkRegion region = this.region;
                this.region = null;
                int posX = RenderChunk.this.x;
                int posY = RenderChunk.this.y;
                int posZ = RenderChunk.this.z;
                if (region != null && !region.isSectionEmpty(posX, posY, posZ)) {
                    EvVisGraph visgraph = GRAPH_CACHE.get();
                    visgraph.reset();
                    PoseStack matrices = MATRICES_CACHE.get().reset();
                    ModelBlockRenderer.enableCaching();
                    IRandom random = RANDOM_CACHE.get();
                    BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
                    for (int dx = 0; dx < 16; ++dx) {
                        int px = posX + dx;
                        for (int dy = 0; dy < 16; ++dy) {
                            int py = posY + dy;
                            for (int dz = 0; dz < 16; ++dz) {
                                int pz = posZ + dz;
                                BlockState blockState = region.getBlockState_(px, py, pz);
                                if (blockState.isSolidRender_(region, px, py, pz)) {
                                    visgraph.setOpaque(dx, dy, dz);
                                }
                                if (blockState.hasBlockEntity()) {
                                    BlockEntity tile = region.getBlockEntity_(px, py, pz);
                                    if (tile != null) {
                                        BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(tile);
                                        if (renderer != null) {
                                            if (renderer.shouldRenderOffScreen(tile)) {
                                                if (blockEntities == null) {
                                                    blockEntities = new RHashSet<>();
                                                }
                                                blockEntities.add(tile);
                                            }
                                            else {
                                                compiledChunk.renderableTEs.add(tile);
                                            }
                                        }
                                    }
                                }
                                FluidState fluidState = region.getFluidState_(px, py, pz);
                                for (int i = RenderLayer.SOLID, len = ChunkBuilderPack.RENDER_TYPES.length; i < len; i++) {
                                    RenderType renderType = ChunkBuilderPack.RENDER_TYPES[i];
                                    if (!fluidState.isEmpty() && ItemBlockRenderTypes.getRenderLayer(fluidState) == renderType) {
                                        BufferBuilder builder = builderPack.builder(i);
                                        if ((compiledChunk.hasLayer & 1 << i) == 0) {
                                            compiledChunk.hasLayer |= (byte) (1 << i);
                                            RenderChunk.this.beginLayer(builder);
                                        }
                                        if (dispatcher.renderLiquid(px, py, pz, region, builder, blockState, fluidState)) {
                                            compiledChunk.hasBlocks |= (byte) (1 << i);
                                        }
                                    }
                                    if (blockState.getRenderShape() != RenderShape.INVISIBLE && ItemBlockRenderTypes.getChunkRenderType(blockState) == renderType) {
                                        BufferBuilder builder = builderPack.builder(i);
                                        if ((compiledChunk.hasLayer & 1 << i) == 0) {
                                            compiledChunk.hasLayer |= (byte) (1 << i);
                                            RenderChunk.this.beginLayer(builder);
                                        }
                                        matrices.pushPose();
                                        matrices.translate(dx, dy, dz);
                                        if (dispatcher.renderBatched(blockState, px, py, pz, region, matrices, builder, true, random)) {
                                            compiledChunk.hasBlocks |= (byte) (1 << i);
                                        }
                                        matrices.popPose();
                                    }
                                }
                            }
                        }
                    }
                    compiledChunk.visibilitySet = visgraph.resolve();
                    if ((compiledChunk.hasBlocks & 1 << RenderLayer.TRANSLUCENT) != 0) {
                        BufferBuilder builder = builderPack.builder(RenderLayer.TRANSLUCENT);
                        builder.setQuadSortOrigin(camX - posX, camY - posY, camZ - posZ);
                        compiledChunk.transparencyState = builder.getSortState();
                    }
                    for (int i = RenderLayer.SOLID, len = ChunkBuilderPack.RENDER_TYPES.length; i < len; i++) {
                        if ((compiledChunk.hasLayer & 1 << i) != 0) {
                            builderPack.builder(i).end();
                        }
                    }
                    ModelBlockRenderer.clearCache();
                }
                else {
                    compiledChunk.visibilitySet = 0b111111_111111_111111_111111_111111_111111L;
                }
                return blockEntities == null ? RSet.emptySet() : blockEntities;
            }

            @Override
            public CompletableFuture<ChunkTaskResult> doTask(ChunkBuilderPack builderPack) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (!RenderChunk.this.hasAllNeighbors()) {
                    this.region = null;
                    RenderChunk.this.setDirty(false);
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                CompiledChunk compiledChunk = new CompiledChunk();
                RenderChunk.this.updateGlobalBlockEntities(this.compile(EvChunkRenderDispatcher.this.camX,
                                                                        EvChunkRenderDispatcher.this.camY,
                                                                        EvChunkRenderDispatcher.this.camZ,
                                                                        compiledChunk, builderPack));
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                OList<CompletableFuture<Void>> list = new OArrayList<>();
                for (int i = RenderLayer.SOLID, len = ChunkBuilderPack.RENDER_TYPES.length; i < len; i++) {
                    if ((compiledChunk.hasLayer & 1 << i) != 0) {
                        list.add(EvChunkRenderDispatcher.this.uploadChunkLayer(builderPack.builder(i), RenderChunk.this.getBuffer(i)));
                    }
                }
                return Util.sequenceFailFast(list).handle((l, t) -> {
                    if (t != null && !(t instanceof CancellationException) && !(t instanceof InterruptedException)) {
                        CrashReport crash = CrashReport.forThrowable(t, "Rendering chunk");
                        Minecraft.getInstance().delayCrash(() -> crash);
                    }
                    if (this.isCancelled.get()) {
                        return ChunkTaskResult.CANCELLED;
                    }
                    RenderChunk.this.compiled = compiledChunk;
                    RenderChunk.this.needsUpdate = true;
                    EvChunkRenderDispatcher.this.renderer.addRecentlyCompiledChunk(RenderChunk.this);
                    return ChunkTaskResult.SUCCESSFUL;
                });
            }

            @Override
            protected String name() {
                return "rend_chk_rebuild";
            }
        }

        class ResortTransparencyTask extends ChunkCompileTask {

            private final CompiledChunk compiledChunk;

            public ResortTransparencyTask(double distAtCreation, CompiledChunk compiledChunk) {
                super(distAtCreation, true);
                this.compiledChunk = compiledChunk;
            }

            @Override
            public void cancel() {
                this.isCancelled.set(true);
            }

            @Override
            public CompletableFuture<ChunkTaskResult> doTask(ChunkBuilderPack builderPack) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (!RenderChunk.this.hasAllNeighbors()) {
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                BufferBuilder.SortState sortState = this.compiledChunk.transparencyState;
                if (sortState != null && (this.compiledChunk.hasBlocks & 1 << RenderLayer.TRANSLUCENT) != 0) {
                    BufferBuilder builder = builderPack.builder(RenderLayer.TRANSLUCENT);
                    RenderChunk.this.beginLayer(builder);
                    builder.restoreSortState(sortState);
                    builder.setQuadSortOrigin(EvChunkRenderDispatcher.this.camX - RenderChunk.this.x,
                                              EvChunkRenderDispatcher.this.camY - RenderChunk.this.y,
                                              EvChunkRenderDispatcher.this.camZ - RenderChunk.this.z);
                    this.compiledChunk.transparencyState = builder.getSortState();
                    builder.end();
                    if (this.isCancelled.get()) {
                        return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                    }
                    CompletableFuture<ChunkTaskResult> future =
                            EvChunkRenderDispatcher.this.uploadChunkLayer(builderPack.builder(RenderLayer.TRANSLUCENT),
                                                                          RenderChunk.this.getBuffer(RenderLayer.TRANSLUCENT))
                                                        .thenApply(v -> ChunkTaskResult.CANCELLED);
                    return future.handle((r, t) -> {
                        if (t != null && !(t instanceof CancellationException) && !(t instanceof InterruptedException)) {
                            CrashReport crash = CrashReport.forThrowable(t, "Rendering chunk");
                            Minecraft.getInstance().delayCrash(() -> crash);
                        }
                        return this.isCancelled.get() ? ChunkTaskResult.CANCELLED : ChunkTaskResult.SUCCESSFUL;
                    });
                }
                return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
            }

            @Override
            protected String name() {
                return "rend_chk_sort";
            }
        }
    }

    public enum ChunkTaskResult {
        SUCCESSFUL,
        CANCELLED
    }
}
