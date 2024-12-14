package tgw.evolution.client.renderer.chunk;

import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.client.renderer.culling.Frustum;
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

@Environment(EnvType.CLIENT)
public class SectionRenderDispatcher {

    private float camX;
    private float camY;
    private float camZ;
    private final Executor executor;
    private final ChunkBuilderPack fixedBuffers;
    private volatile int freeBufferCount;
    private final Queue<ChunkBuilderPack> freeBuffers;
    private final VisGraph graph = new VisGraph();
    private int highPriorityQuota = 2;
    private ClientLevel level;
    private final ProcessorMailbox<Runnable> mailbox;
    private final PoseStack matrices = new PoseStack();
    private final IRandom random = new FastRandom();
    private final LevelRenderer renderer;
    private volatile int toBatchCount;
    private final PriorityBlockingQueue<RenderSection.ChunkCompileTask> toBatchHighPriority = new PriorityBlockingQueue<>();
    private final Queue<RenderSection.ChunkCompileTask> toBatchLowPriority = new LinkedBlockingDeque<>();
    private final Queue<Runnable> toUpload = new ConcurrentLinkedQueue<>();

    public SectionRenderDispatcher(ClientLevel level, LevelRenderer renderer, Executor executor, boolean is64Bit, ChunkBuilderPack builderPack) {
        this(level, renderer, executor, is64Bit, builderPack, -1);
    }

    public SectionRenderDispatcher(ClientLevel level, LevelRenderer renderer, Executor executor, boolean is64Bit, ChunkBuilderPack builderPack, int countRenderBuilders) {
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
            RenderSection.ChunkCompileTask compileTask = this.toBatchHighPriority.poll();
            if (compileTask != null) {
                compileTask.cancel();
            }
        }
        while (!this.toBatchLowPriority.isEmpty()) {
            RenderSection.ChunkCompileTask compileTask = this.toBatchLowPriority.poll();
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

    private @Nullable SectionRenderDispatcher.RenderSection.ChunkCompileTask pollTask() {
        if (this.highPriorityQuota <= 0) {
            RenderSection.ChunkCompileTask compileTask = this.toBatchLowPriority.poll();
            if (compileTask != null) {
                this.highPriorityQuota = 2;
                return compileTask;
            }
        }
        RenderSection.ChunkCompileTask compileTask = this.toBatchHighPriority.poll();
        if (compileTask != null) {
            --this.highPriorityQuota;
            return compileTask;
        }
        this.highPriorityQuota = 2;
        return this.toBatchLowPriority.poll();
    }

    public void rebuildChunkSync(RenderSection chunk) {
        chunk.compileSync();
    }

    private void runTask() {
        if (!this.freeBuffers.isEmpty()) {
            RenderSection.ChunkCompileTask compileTask = this.pollTask();
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
    private void schedule(RenderSection.ChunkCompileTask compileTask) {
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

    public static class CompiledSection {
        public static final CompiledSection EMPTY = new CompiledSection() {
            @Override
            public boolean facesCanSeeEachother(Direction face, Direction other) {
                return true;
            }

            @Override
            public boolean isEmpty(@RenderLayer int renderType) {
                return true;
            }
        };
        public static final CompiledSection UNCOMPILED = new CompiledSection() {
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

    @Environment(EnvType.CLIENT)
    public class RenderSection implements Octree.Node {
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
        protected volatile CompiledSection compiled = CompiledSection.UNCOMPILED;
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
        private long sectionPos = SectionPos.asLong(-1, -1, -1);
        /**
         * Only access from the Main Thread.
         */
        public @Visibility int visibility;
        private int x;
        private int y;
        private int z;

        public RenderSection(int index, long secPos) {
            for (int i = 0, len = this.buffers.length; i < len; i++) {
                //noinspection ObjectAllocationInLoop,resource
                this.buffers[i] = new VertexBuffer();
            }
            this.index = index;
            this.setSectionNode(secPos);
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
            LevelChunk chunk = SectionRenderDispatcher.this.level.getChunk(chunkX, chunkZ);
            if (chunk.isYSpaceEmpty(this.y, this.y + 15)) {
                RenderSection.this.updateGlobalBlockEntities(RSet.emptySet());
                RenderSection.this.compiled = CompiledSection.EMPTY;
                RenderSection.this.cachedFlags = 0;
                SectionRenderDispatcher.this.renderer.addRecentlyCompiledChunk(RenderSection.this);
                return;
            }
            CompiledSection oldChunk = this.compiled;
            CompiledSection compiledSection;
            if (oldChunk == CompiledSection.UNCOMPILED || oldChunk == CompiledSection.EMPTY) {
                compiledSection = new CompiledSection();
            }
            else {
                compiledSection = oldChunk;
                compiledSection.visibilitySet = 0;
                compiledSection.hasBlocks = 0;
                compiledSection.hasLayer = 0;
                compiledSection.renderableTEs.clear();
                compiledSection.transparencyState = null;
            }
            ChunkBuilderPack fixedBuffers = SectionRenderDispatcher.this.fixedBuffers;
            RSet<BlockEntity> blockEntities = null;
            int posX = this.x;
            int posY = this.y;
            int posZ = this.z;
            VisGraph visgraph = SectionRenderDispatcher.this.graph;
            visgraph.reset();
            PoseStack matrices = SectionRenderDispatcher.this.matrices.reset();
            ModelBlockRenderer.enableCaching();
            IRandom random = SectionRenderDispatcher.this.random;
            BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            for (int dx = 0; dx < 16; ++dx) {
                int px = posX + dx;
                for (int dy = 0; dy < 16; ++dy) {
                    int py = posY + dy;
                    for (int dz = 0; dz < 16; ++dz) {
                        int pz = posZ + dz;
                        BlockState blockState = chunk.getBlockState_(px, py, pz);
                        if (blockState.isSolidRender_(SectionRenderDispatcher.this.level, px, py, pz)) {
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
                                        compiledSection.renderableTEs.add(tile);
                                    }
                                }
                            }
                        }
                        FluidState fluidState = chunk.getFluidState_(px, py, pz);
                        for (int i = RenderLayer.SOLID, len = ChunkBuilderPack.RENDER_TYPES.length; i < len; i++) {
                            RenderType renderType = ChunkBuilderPack.RENDER_TYPES[i];
                            if (!fluidState.isEmpty() && ItemBlockRenderTypes.getRenderLayer(fluidState) == renderType) {
                                BufferBuilder builder = fixedBuffers.builder(i);
                                if ((compiledSection.hasLayer & 1 << i) == 0) {
                                    compiledSection.hasLayer |= (byte) (1 << i);
                                    RenderSection.this.beginLayer(builder);
                                }
                                if (dispatcher.renderLiquid(px, py, pz, SectionRenderDispatcher.this.level, builder, blockState, fluidState)) {
                                    compiledSection.hasBlocks |= (byte) (1 << i);
                                }
                            }
                            if (blockState.getRenderShape() != RenderShape.INVISIBLE &&
                                ItemBlockRenderTypes.getChunkRenderType(blockState) == renderType) {
                                BufferBuilder builder = fixedBuffers.builder(i);
                                if ((compiledSection.hasLayer & 1 << i) == 0) {
                                    compiledSection.hasLayer |= (byte) (1 << i);
                                    RenderSection.this.beginLayer(builder);
                                }
                                matrices.pushPose();
                                matrices.translate(dx, dy, dz);
                                if (dispatcher.renderBatched(blockState, px, py, pz, SectionRenderDispatcher.this.level, matrices, builder, true, random)) {
                                    compiledSection.hasBlocks |= (byte) (1 << i);
                                }
                                matrices.popPose();
                            }
                        }
                    }
                }
            }
            compiledSection.visibilitySet = visgraph.resolve();
            if ((compiledSection.hasBlocks & 1 << RenderLayer.TRANSLUCENT) != 0) {
                BufferBuilder builder = fixedBuffers.builder(RenderLayer.TRANSLUCENT);
                builder.setQuadSortOrigin(SectionRenderDispatcher.this.camX - posX, SectionRenderDispatcher.this.camY - posY, SectionRenderDispatcher.this.camZ - posZ);
                compiledSection.transparencyState = builder.getSortState();
            }
            for (int i = RenderLayer.SOLID, len = ChunkBuilderPack.RENDER_TYPES.length; i < len; i++) {
                if ((compiledSection.hasLayer & 1 << i) != 0) {
                    fixedBuffers.builder(i).end();
                }
            }
            ModelBlockRenderer.clearCache();
            blockEntities = blockEntities == null ? RSet.emptySet() : blockEntities;
            RenderSection.this.updateGlobalBlockEntities(blockEntities);
            for (int i = RenderLayer.SOLID, len = ChunkBuilderPack.RENDER_TYPES.length; i < len; i++) {
                if ((compiledSection.hasLayer & 1 << i) != 0) {
                    RenderSection.this.getBuffer(i).upload(fixedBuffers.builder(i));
                }
            }
            RenderSection.this.compiled = compiledSection;
            RenderSection.this.needsUpdate = true;
            SectionRenderDispatcher.this.renderer.addRecentlyCompiledChunk(RenderSection.this);
        }

        private boolean doesChunkExistAt(int posX, int posZ) {
            return SectionRenderDispatcher.this.level.getChunk(SectionPos.blockToSectionCoord(posX), SectionPos.blockToSectionCoord(posZ), ChunkStatus.FULL, false) != null;
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

        public long getNeighborSectionNode(Direction direction) {
            return SectionPos.offset(this.sectionPos, direction);
        }

        public byte getRenderLayers() {
            if (this.needsUpdate) {
                this.updateCache();
            }
            return (byte) (this.cachedFlags & 0b1_1111);
        }

        @Override
        public @Nullable SectionRenderDispatcher.RenderSection getSection() {
            return this;
        }

        public long getSectionPos() {
            return this.sectionPos;
        }

        public int getX() {
            return this.x;
        }

        @Override
        public double getX0() {
            return this.x;
        }

        @Override
        public double getX1() {
            return this.x + 16;
        }

        public int getY() {
            return this.y;
        }

        @Override
        public double getY0() {
            return this.y;
        }

        @Override
        public double getY1() {
            return this.y + 16;
        }

        public int getZ() {
            return this.z;
        }

        @Override
        public double getZ0() {
            return this.z;
        }

        @Override
        public double getZ1() {
            return this.z + 16;
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

        public boolean isUncompiled() {
            if (this.needsUpdate) {
                this.updateCache();
            }
            return (this.cachedFlags & 1 << 7) != 0;
        }

        /**
         * This method only runs on the Main Thread.
         */
        public void rebuildChunkAsync(SectionRenderDispatcher dispatcher, RenderRegionCache cache) {
            boolean canceled = this.cancelTasks();
            RenderChunkRegion region = cache.createRegion(SectionRenderDispatcher.this.level, this.x - 1, this.y - 1, this.z - 1, this.x + 16, this.y + 16, this.z + 16, 1);
            if (region == null) {
                RenderSection.this.updateGlobalBlockEntities(RSet.emptySet());
                RenderSection.this.cachedFlags = 0;
                RenderSection.this.compiled = CompiledSection.EMPTY;
                SectionRenderDispatcher.this.renderer.addRecentlyCompiledChunk(RenderSection.this);
                return;
            }
            this.lastRebuildTask = new RenderSection.RebuildTask(this.getDistToCameraSqr(), region, canceled || this.compiled != CompiledSection.UNCOMPILED);
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
            this.compiled = CompiledSection.UNCOMPILED;
            this.cachedFlags = (byte) (1 << 7);
            this.dirty = true;
        }

        /**
         * This method only runs on the Main Thread.
         */
        public boolean resortTransparency(SectionRenderDispatcher dispatcher, boolean onThread) {
            if (this.lastResortTransparencyTask != null) {
                this.lastResortTransparencyTask.cancel();
            }
            if (this.isEmpty(RenderLayer.TRANSLUCENT)) {
                return false;
            }
            if (onThread) {
                if (!RenderSection.this.hasAllNeighbors()) {
                    return true;
                }
                CompiledSection compiled = this.compiled;
                BufferBuilder.SortState sortState = compiled.transparencyState;
                if (sortState != null) {
                    BufferBuilder builder = SectionRenderDispatcher.this.fixedBuffers.builder(RenderLayer.TRANSLUCENT);
                    RenderSection.this.beginLayer(builder);
                    builder.restoreSortState(sortState);
                    builder.setQuadSortOrigin(SectionRenderDispatcher.this.camX - RenderSection.this.x, SectionRenderDispatcher.this.camY - RenderSection.this.y, SectionRenderDispatcher.this.camZ - RenderSection.this.z);
                    compiled.transparencyState = builder.getSortState();
                    builder.end();
                    RenderSection.this.getBuffer(RenderLayer.TRANSLUCENT).upload(SectionRenderDispatcher.this.fixedBuffers.builder(RenderLayer.TRANSLUCENT));
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
        public void setSectionNode(long secPos) {
            int x = SectionPos.sectionToBlockCoord(SectionPos.x(secPos));
            int y = SectionPos.sectionToBlockCoord(SectionPos.y(secPos));
            int z = SectionPos.sectionToBlockCoord(SectionPos.z(secPos));
            if (EarthHelper.wrapBlockCoordinate(this.x) != EarthHelper.wrapBlockCoordinate(x) || this.y != y || EarthHelper.wrapBlockCoordinate(this.z) != EarthHelper.wrapBlockCoordinate(z)) {
                this.reset();
            }
            this.sectionPos = secPos;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * This method only runs on the Main Thread.
         */
        private void updateCache() {
            this.needsUpdate = false;
            CompiledSection compiled = this.compiled;
            this.cachedFlags = compiled.hasBlocks;
            if (compiled == CompiledSection.UNCOMPILED) {
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
                SectionRenderDispatcher.this.renderer.updateGlobalBlockEntities(toRemove, toAdd);
            }
            else {
                SectionRenderDispatcher.this.renderer.updateGlobalBlockEntities(this.globalBlockEntities, blockEntities);
            }
        }

        @Override
        public int visitNodes(Octree.OctreeVisitor octreeVisitor, @Visibility int visibility, Frustum frustum, int depth, int count) {
            if (visibility == Visibility.INSIDE) {
                return octreeVisitor.visit(this, visibility, depth, count);
            }
            ++count;
            if ((visibility = frustum.intersectWith(this.getX0(), this.getY0(), this.getZ0(), this.getX1(), this.getY1(), this.getZ1())) > Visibility.OUTSIDE) {
                return octreeVisitor.visit(this, visibility, depth, count);
            }
            return count;
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

            static final ThreadLocal<VisGraph> GRAPH_CACHE = ThreadLocal.withInitial(VisGraph::new);
            static final ThreadLocal<PoseStack> MATRICES_CACHE = ThreadLocal.withInitial(PoseStack::new);
            static final ThreadLocal<IRandom> RANDOM_CACHE = ThreadLocal.withInitial(FastRandom::new);
            protected @Nullable RenderChunkRegion region;

            public RebuildTask(double distAtCreation, @Nullable RenderChunkRegion region, boolean isHighPriority) {
                super(distAtCreation, isHighPriority);
                this.region = region;
            }

            @Override
            public void cancel() {
                this.region = null;
                if (this.isCancelled.compareAndSet(false, true)) {
                    RenderSection.this.setDirty(false);
                }
            }

            private RSet<BlockEntity> compile(float camX, float camY, float camZ, CompiledSection compiledSection, ChunkBuilderPack builderPack) {
                RSet<BlockEntity> blockEntities = null;
                RenderChunkRegion region = this.region;
                this.region = null;
                int posX = RenderSection.this.x;
                int posY = RenderSection.this.y;
                int posZ = RenderSection.this.z;
                if (region != null && !region.isSectionEmpty(posX, posY, posZ)) {
                    VisGraph visgraph = GRAPH_CACHE.get();
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
                                                compiledSection.renderableTEs.add(tile);
                                            }
                                        }
                                    }
                                }
                                FluidState fluidState = region.getFluidState_(px, py, pz);
                                for (int i = RenderLayer.SOLID, len = ChunkBuilderPack.RENDER_TYPES.length; i < len; i++) {
                                    RenderType renderType = ChunkBuilderPack.RENDER_TYPES[i];
                                    if (!fluidState.isEmpty() && ItemBlockRenderTypes.getRenderLayer(fluidState) == renderType) {
                                        BufferBuilder builder = builderPack.builder(i);
                                        if ((compiledSection.hasLayer & 1 << i) == 0) {
                                            compiledSection.hasLayer |= (byte) (1 << i);
                                            RenderSection.this.beginLayer(builder);
                                        }
                                        if (dispatcher.renderLiquid(px, py, pz, region, builder, blockState, fluidState)) {
                                            compiledSection.hasBlocks |= (byte) (1 << i);
                                        }
                                    }
                                    if (blockState.getRenderShape() != RenderShape.INVISIBLE && ItemBlockRenderTypes.getChunkRenderType(blockState) == renderType) {
                                        BufferBuilder builder = builderPack.builder(i);
                                        if ((compiledSection.hasLayer & 1 << i) == 0) {
                                            compiledSection.hasLayer |= (byte) (1 << i);
                                            RenderSection.this.beginLayer(builder);
                                        }
                                        matrices.pushPose();
                                        matrices.translate(dx, dy, dz);
                                        if (dispatcher.renderBatched(blockState, px, py, pz, region, matrices, builder, true, random)) {
                                            compiledSection.hasBlocks |= (byte) (1 << i);
                                        }
                                        matrices.popPose();
                                    }
                                }
                            }
                        }
                    }
                    compiledSection.visibilitySet = visgraph.resolve();
                    if ((compiledSection.hasBlocks & 1 << RenderLayer.TRANSLUCENT) != 0) {
                        BufferBuilder builder = builderPack.builder(RenderLayer.TRANSLUCENT);
                        builder.setQuadSortOrigin(camX - posX, camY - posY, camZ - posZ);
                        compiledSection.transparencyState = builder.getSortState();
                    }
                    for (int i = RenderLayer.SOLID, len = ChunkBuilderPack.RENDER_TYPES.length; i < len; i++) {
                        if ((compiledSection.hasLayer & 1 << i) != 0) {
                            builderPack.builder(i).end();
                        }
                    }
                    ModelBlockRenderer.clearCache();
                }
                else {
                    compiledSection.visibilitySet = 0b111111_111111_111111_111111_111111_111111L;
                }
                return blockEntities == null ? RSet.emptySet() : blockEntities;
            }

            @Override
            public CompletableFuture<ChunkTaskResult> doTask(ChunkBuilderPack builderPack) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (!RenderSection.this.hasAllNeighbors()) {
                    this.region = null;
                    RenderSection.this.setDirty(false);
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                CompiledSection compiledSection = new CompiledSection();
                RenderSection.this.updateGlobalBlockEntities(this.compile(SectionRenderDispatcher.this.camX, SectionRenderDispatcher.this.camY, SectionRenderDispatcher.this.camZ, compiledSection, builderPack));
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                OList<CompletableFuture<Void>> list = new OArrayList<>();
                for (int i = RenderLayer.SOLID, len = ChunkBuilderPack.RENDER_TYPES.length; i < len; i++) {
                    if ((compiledSection.hasLayer & 1 << i) != 0) {
                        list.add(SectionRenderDispatcher.this.uploadChunkLayer(builderPack.builder(i), RenderSection.this.getBuffer(i)));
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
                    RenderSection.this.compiled = compiledSection;
                    RenderSection.this.needsUpdate = true;
                    SectionRenderDispatcher.this.renderer.addRecentlyCompiledChunk(RenderSection.this);
                    return ChunkTaskResult.SUCCESSFUL;
                });
            }

            @Override
            protected String name() {
                return "rend_chk_rebuild";
            }
        }

        class ResortTransparencyTask extends ChunkCompileTask {

            private final CompiledSection compiledSection;

            public ResortTransparencyTask(double distAtCreation, CompiledSection compiledSection) {
                super(distAtCreation, true);
                this.compiledSection = compiledSection;
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
                if (!RenderSection.this.hasAllNeighbors()) {
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                BufferBuilder.SortState sortState = this.compiledSection.transparencyState;
                if (sortState != null && (this.compiledSection.hasBlocks & 1 << RenderLayer.TRANSLUCENT) != 0) {
                    BufferBuilder builder = builderPack.builder(RenderLayer.TRANSLUCENT);
                    RenderSection.this.beginLayer(builder);
                    builder.restoreSortState(sortState);
                    builder.setQuadSortOrigin(SectionRenderDispatcher.this.camX - RenderSection.this.x,
                                              SectionRenderDispatcher.this.camY - RenderSection.this.y,
                                              SectionRenderDispatcher.this.camZ - RenderSection.this.z);
                    this.compiledSection.transparencyState = builder.getSortState();
                    builder.end();
                    if (this.isCancelled.get()) {
                        return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                    }
                    CompletableFuture<ChunkTaskResult> future =
                            SectionRenderDispatcher.this.uploadChunkLayer(builderPack.builder(RenderLayer.TRANSLUCENT),
                                                                          RenderSection.this.getBuffer(RenderLayer.TRANSLUCENT))
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
