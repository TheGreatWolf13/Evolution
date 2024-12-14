package tgw.evolution.client.renderer.chunk;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.L2OHashMap;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.collection.queues.OArrayBlockingQueue;
import tgw.evolution.util.collection.queues.OArrayQueue;
import tgw.evolution.util.collection.queues.OQueue;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.collection.sets.LSet;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.VectorUtil;

import java.util.Comparator;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Environment(EnvType.CLIENT)
public class SectionOcclusionGraph {
    private static final int MINIMUM_ADVANCED_CULLING_DISTANCE = 60;
    private static final double CEILED_SECTION_DIAGONAL = Math.ceil(Math.sqrt(3.0) * 16.0);
    private static final ThreadLocal<OArrayQueue<Node>> QUEUE_CACHE = ThreadLocal.withInitial(OArrayQueue::new);
    private final AtomicReference<GraphState> currentGraph = new AtomicReference<>();
    private final LHashSet emptyLoadedSections = new LHashSet();
    private int frustumChecks;
    private @Nullable Future<?> fullUpdateTask;
    private final LevelRenderer levelRenderer;
    private final AtomicBoolean needsFrustumUpdate = new AtomicBoolean(true);
    private boolean needsFullUpdate = true;
    private final AtomicReference<SectionOcclusionGraph.GraphEvents> nextGraphEvents = new AtomicReference<>();
    private @Nullable ViewArea viewArea;

    public SectionOcclusionGraph(LevelRenderer levelRenderer) {
        this.levelRenderer = levelRenderer;
    }

    private static void addNeighbors(SectionOcclusionGraph.GraphEvents graphEvents, int chunkX, int chunkZ) {
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkX - 1, chunkZ));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkX, chunkZ - 1));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkX + 1, chunkZ));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkX, chunkZ + 1));
    }

    private static boolean isInViewDistance(int camSecX, int camSecZ, int renderDistance, int secX, int secZ) {
        int n = Math.max(0, Math.abs(secX - camSecX) - 1);
        int o = Math.max(0, Math.abs(secZ - camSecZ) - 1);
        long p = Math.max(n, o);
        long q = Math.min(n, o);
        long r = q * q + p * p;
        int s = renderDistance * renderDistance;
        return r < s;
    }

    public void addSectionsInFrustum(Frustum frustum, OList<SectionRenderDispatcher.RenderSection> list) {
        this.frustumChecks = this.currentGraph.get().storage().sectionTree.visitNodes((node, visibility, depth, count) -> {
            SectionRenderDispatcher.RenderSection section = node.getSection();
            if (section != null) {
                section.visibility = visibility;
                list.add(section);
                this.levelRenderer.updateLayersInFrustum(section.getRenderLayers());
            }
            return count;
        }, frustum);
    }

    public boolean consumeFrustumUpdate() {
        return this.needsFrustumUpdate.compareAndSet(true, false);
    }

    public int getNumberOfFrustumChecks() {
        return this.frustumChecks;
    }

    public Octree getOctree() {
        return this.currentGraph.get().storage.sectionTree;
    }

    private @Nullable SectionRenderDispatcher.RenderSection getRelativeFrom(long secPos, SectionRenderDispatcher.RenderSection renderSection, Direction direction) {
        assert this.viewArea != null;
        long m = renderSection.getNeighborSectionNode(direction);
        if (!this.isInViewDistance(secPos, m)) {
            return null;
        }
        return Mth.abs(SectionPos.y(secPos) - SectionPos.y(m)) > this.viewArea.getRenderDistance() ? null : this.viewArea.getRenderSection(m);
    }

    private void initializeQueueForFullUpdate(Camera camera, OQueue<Node> queue) {
        assert this.viewArea != null;
        BlockPos camBlockPos = camera.getBlockPosition();
        long camSec = SectionPos.asLong(camBlockPos);
        int camSecY = SectionPos.y(camSec);
        SectionRenderDispatcher.RenderSection renderSection = this.viewArea.getRenderSection(camSec);
        if (renderSection == null) {
            Level level = this.viewArea.level;
            boolean belowWorld = camSecY < level.getMinSection();
            int secY = belowWorld ? level.getMinSection() : level.getMaxSection();
            int renderDistance = this.viewArea.getRenderDistance();
            OList<Node> list = new OArrayList<>();
            int camSecX = SectionPos.x(camSec);
            int camSecZ = SectionPos.z(camSec);
            for (int dx = -renderDistance; dx <= renderDistance; dx++) {
                for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                    SectionRenderDispatcher.RenderSection sectionAt = this.viewArea.getRenderSection(SectionPos.asLong(dx + camSecX, secY, dz + camSecZ));
                    if (sectionAt != null && this.isInViewDistance(camSec, sectionAt.getSectionPos())) {
                        Direction direction = belowWorld ? Direction.UP : Direction.DOWN;
                        //noinspection ObjectAllocationInLoop
                        SectionOcclusionGraph.Node node = new SectionOcclusionGraph.Node(sectionAt, direction, 0);
                        node.setDirections(node.directions, direction);
                        if (dx > 0) {
                            node.setDirections(node.directions, Direction.EAST);
                        }
                        else if (dx < 0) {
                            node.setDirections(node.directions, Direction.WEST);
                        }
                        if (dz > 0) {
                            node.setDirections(node.directions, Direction.SOUTH);
                        }
                        else if (dz < 0) {
                            node.setDirections(node.directions, Direction.NORTH);
                        }
                        list.add(node);
                    }
                }
            }
            list.sort(Comparator.comparingDouble(n -> camBlockPos.distToLowCornerSqr(n.section.getX() + 8, n.section.getY() + 8, n.section.getZ() + 8)));
            queue.enqueueMany(list);
        }
        else {
            queue.enqueue(new SectionOcclusionGraph.Node(renderSection, null, 0));
        }
    }

    public void invalidate() {
        this.needsFullUpdate = true;
    }

    private boolean isInViewDistance(long camSec, long sec) {
        assert this.viewArea != null;
        return isInViewDistance(SectionPos.x(camSec), SectionPos.z(camSec), this.viewArea.getRenderDistance(), SectionPos.x(sec), SectionPos.z(sec));
    }

    public void onChunkLoaded(int chunkX, int chunkZ) {
        SectionOcclusionGraph.GraphEvents nextEvents = this.nextGraphEvents.get();
        if (nextEvents != null) {
            addNeighbors(nextEvents, chunkX, chunkZ);
        }
        SectionOcclusionGraph.GraphEvents currentEvents = this.currentGraph.get().events;
        if (currentEvents != nextEvents) {
            addNeighbors(currentEvents, chunkX, chunkZ);
        }
    }

    private void queueSectionsWithNewNeighbors(SectionOcclusionGraph.GraphState graphState) {
        LSet chunksWhichReceivedNeighbors = graphState.events.chunksWhichReceivedNeighbors;
        for (long it = chunksWhichReceivedNeighbors.beginIteration(); chunksWhichReceivedNeighbors.hasNextIteration(it); it = chunksWhichReceivedNeighbors.nextEntry(it)) {
            long pos = chunksWhichReceivedNeighbors.getIteration(it);
            OList<SectionRenderDispatcher.RenderSection> list = graphState.storage.chunksWaitingForNeighbors.get(pos);
            if (list != null && list.get(0).hasAllNeighbors()) {
                graphState.events.sectionsToPropagateFrom.enqueueMany(list);
                graphState.storage.chunksWaitingForNeighbors.remove(pos);
            }
        }
        chunksWhichReceivedNeighbors.clear();
    }

    private void runPartialUpdate(boolean smartCull, Frustum frustum, OList<SectionRenderDispatcher.RenderSection> sectionsInFrustum, Vec3 camPos, LSet emptyLoadedSections) {
        GraphState graphState = this.currentGraph.get();
        this.queueSectionsWithNewNeighbors(graphState);
        if (!graphState.events.sectionsToPropagateFrom.isEmpty()) {
            OArrayQueue<Node> queue = QUEUE_CACHE.get();
            assert queue.isEmpty();
            while (!graphState.events.sectionsToPropagateFrom.isEmpty()) {
                SectionRenderDispatcher.RenderSection section = graphState.events.sectionsToPropagateFrom.dequeue();
                Node node = graphState.storage.sectionToNodeMap.get(section);
                if (node != null && node.section == section) {
                    queue.enqueue(node);
                }
            }
            this.runUpdates(graphState.storage, camPos, queue, smartCull, frustum.offsetToFullyIncludeCameraCube(8), sectionsInFrustum, emptyLoadedSections);
            queue.clear();
        }
    }

    private void runUpdates(GraphStorage graphStorage, Vec3 camPos, OArrayQueue<Node> queue, boolean smartCull, @Nullable Frustum frustum, @Nullable OList<SectionRenderDispatcher.RenderSection> sectionsInFrustum, LSet emptyLoadedSections) {
        assert this.viewArea != null;
        Level level = this.viewArea.level;
        int cameraX = Mth.floor(camPos.x / 16) * 16;
        int cameraY = Mth.floor(camPos.y / 16) * 16;
        int cameraZ = Mth.floor(camPos.z / 16) * 16;
        long camSec = SectionPos.asLong(SectionPos.blockToSectionCoord(cameraX), SectionPos.blockToSectionCoord(cameraY), SectionPos.blockToSectionCoord(cameraZ));
        int centerX = cameraX + 8;
        int centerY = cameraY + 8;
        int centerZ = cameraZ + 8;
        Entity.setViewScale(Mth.clamp(Minecraft.getInstance().options.getEffectiveRenderDistance() / 8.0, 1, 2.5) * Minecraft.getInstance().options.entityDistanceScaling);
        while (!queue.isEmpty()) {
            Node node = queue.dequeue();
            SectionRenderDispatcher.RenderSection section = node.section;
            if (!emptyLoadedSections.contains(section.getSectionPos())) {
                if (graphStorage.sectionTree.add(section)) {
                    if (frustum != null) {
                        assert sectionsInFrustum != null;
                        int x = section.getX();
                        int y = section.getY();
                        int z = section.getZ();
                        if (frustum.cubeInFrustum(x, y, z, x + 16, y + 16, z + 16)) {
                            sectionsInFrustum.add(section);
                        }
                    }
                }
            }
            else {
                section.compiled = SectionRenderDispatcher.CompiledSection.EMPTY;
            }
            int secX = section.getX();
            int secY = section.getY();
            int secZ = section.getZ();
            boolean far = Math.abs(secX - cameraX) > MINIMUM_ADVANCED_CULLING_DISTANCE || Math.abs(secY - cameraY) > MINIMUM_ADVANCED_CULLING_DISTANCE || Math.abs(secZ - cameraZ) > MINIMUM_ADVANCED_CULLING_DISTANCE;
            directions:
            for (Direction dir : DirectionUtil.ALL) {
                SectionRenderDispatcher.RenderSection sectionAtDir = this.getRelativeFrom(camSec, section, dir);
                if (sectionAtDir != null && (!smartCull || !node.hasDirection(dir.getOpposite()))) {
                    if (smartCull && node.hasSourceDirections()) {
                        SectionRenderDispatcher.CompiledSection compiled = section.compiled;
                        boolean cull = false;
                        for (Direction dirForCull : DirectionUtil.ALL) {
                            if (node.hasSourceDirection(dirForCull) && compiled.facesCanSeeEachother(dirForCull.getOpposite(), dir)) {
                                cull = true;
                                break;
                            }
                        }
                        if (!cull) {
                            continue;
                        }
                    }
                    if (smartCull && far) {
                        int dx = 0;
                        int dy = 0;
                        int dz = 0;
                        switch (dir.getAxis()) {
                            case X -> {
                                if (centerX > sectionAtDir.getX()) {
                                    dx = 16;
                                }
                                if (centerY < sectionAtDir.getY()) {
                                    dy = 16;
                                }
                                if (centerZ < sectionAtDir.getZ()) {
                                    dz = 16;
                                }
                            }
                            case Y -> {
                                if (centerX < sectionAtDir.getX()) {
                                    dx = 16;
                                }
                                if (centerY > sectionAtDir.getY()) {
                                    dy = 16;
                                }
                                if (centerZ < sectionAtDir.getZ()) {
                                    dz = 16;
                                }
                            }
                            case Z -> {
                                if (centerX < sectionAtDir.getX()) {
                                    dx = 16;
                                }
                                if (centerY < sectionAtDir.getY()) {
                                    dy = 16;
                                }
                                if (centerZ > sectionAtDir.getZ()) {
                                    dz = 16;
                                }
                            }
                        }
                        double sectionAtDirX = sectionAtDir.getX() + dx;
                        double sectionAtDirY = sectionAtDir.getY() + dy;
                        double sectionAtDirZ = sectionAtDir.getZ() + dz;
                        double deltaX = camPos.x - sectionAtDirX;
                        double deltaY = camPos.y - sectionAtDirY;
                        double deltaZ = camPos.z - sectionAtDirZ;
                        double norm = VectorUtil.norm(deltaX, deltaY, deltaZ) * CEILED_SECTION_DIAGONAL;
                        deltaX *= norm;
                        deltaY *= norm;
                        deltaZ *= norm;
                        while (VectorUtil.subtractLengthSqr(camPos, sectionAtDirX, sectionAtDirY, sectionAtDirZ) > MINIMUM_ADVANCED_CULLING_DISTANCE * MINIMUM_ADVANCED_CULLING_DISTANCE) {
                            sectionAtDirX += deltaX;
                            sectionAtDirY += deltaY;
                            sectionAtDirZ += deltaZ;
                            if (sectionAtDirY > level.getMaxBuildHeight() || sectionAtDirY < level.getMinBuildHeight()) {
                                break;
                            }
                            SectionRenderDispatcher.RenderSection sectionAt = this.viewArea.getRenderSectionAt(Mth.floor(sectionAtDirX), Mth.floor(sectionAtDirY), Mth.floor(sectionAtDirZ));
                            if (sectionAt == null || graphStorage.sectionToNodeMap.get(sectionAt) == null) {
                                continue directions;
                            }
                        }
                    }
                    Node nodeAtDir = graphStorage.sectionToNodeMap.get(sectionAtDir);
                    if (nodeAtDir != null) {
                        nodeAtDir.addSourceDirection(dir);
                    }
                    else {
                        //noinspection ObjectAllocationInLoop
                        Node newNode = new Node(sectionAtDir, dir, node.step + 1);
                        newNode.setDirections(node.directions, dir);
                        if (sectionAtDir.hasAllNeighbors()) {
                            queue.enqueue(newNode);
                            graphStorage.sectionToNodeMap.put(sectionAtDir, newNode);
                        }
                        else if (this.isInViewDistance(camSec, sectionAtDir.getSectionPos())) {
                            graphStorage.sectionToNodeMap.put(sectionAtDir, newNode);
                            long secAtDir = ChunkPos.asLong(SectionPos.blockToSectionCoord(sectionAtDir.getX()), SectionPos.blockToSectionCoord(sectionAtDir.getZ()));
                            if (!graphStorage.chunksWaitingForNeighbors.containsKey(secAtDir)) {
                                //noinspection ObjectAllocationInLoop
                                OList<SectionRenderDispatcher.RenderSection> list = new OArrayList<>();
                                list.add(sectionAtDir);
                                graphStorage.chunksWaitingForNeighbors.put(secAtDir, list);
                            }
                        }
                    }
                }
            }
        }
    }

    private void scheduleFullUpdate(boolean smartCull, Camera camera, Vec3 camPos, LHashSet emptyLoadedSections) {
        assert this.viewArea != null;
        this.needsFullUpdate = false;
        this.emptyLoadedSections.loadFrom(emptyLoadedSections);
        this.fullUpdateTask = Util.backgroundExecutor().submit(() -> {
            SectionOcclusionGraph.GraphState graphState = new SectionOcclusionGraph.GraphState(this.viewArea);
            this.nextGraphEvents.set(graphState.events);
            OArrayQueue<Node> queue = QUEUE_CACHE.get();
            assert queue.isEmpty();
            this.initializeQueueForFullUpdate(camera, queue);
            for (long it = queue.beginIteration(); queue.hasNextIteration(it); it = queue.nextEntry(it)) {
                Node node = queue.getIteration(it);
                graphState.storage.sectionToNodeMap.put(node.section, node);
            }
            this.runUpdates(graphState.storage, camPos, queue, smartCull, null, null, this.emptyLoadedSections);
            this.currentGraph.set(graphState);
            this.nextGraphEvents.set(null);
            this.needsFrustumUpdate.set(true);
            queue.clear();
        });
    }

    public void schedulePropagationFrom(SectionRenderDispatcher.RenderSection section) {
        SectionOcclusionGraph.GraphEvents nextEvents = this.nextGraphEvents.get();
        if (nextEvents != null) {
            nextEvents.sectionsToPropagateFrom.enqueue(section);
        }
        SectionOcclusionGraph.GraphEvents currentEvents = this.currentGraph.get().events;
        if (currentEvents != nextEvents) {
            currentEvents.sectionsToPropagateFrom.enqueue(section);
        }
    }

    public void update(boolean smartCull, Camera camera, Frustum frustum, OList<SectionRenderDispatcher.RenderSection> list, LHashSet loadedEmptySections) {
        Vec3 camPos = camera.getPosition();
        if (this.needsFullUpdate && (this.fullUpdateTask == null || this.fullUpdateTask.isDone())) {
            this.scheduleFullUpdate(smartCull, camera, camPos, loadedEmptySections);
        }
        this.runPartialUpdate(smartCull, frustum, list, camPos, loadedEmptySections);
    }

    public void waitAndReset(@Nullable ViewArea viewArea) {
        if (this.fullUpdateTask != null) {
            try {
                this.fullUpdateTask.get();
                this.fullUpdateTask = null;
            }
            catch (Exception e) {
                Evolution.warn("Full update failed: {}", e);
            }
        }
        this.viewArea = viewArea;
        if (viewArea != null) {
            this.currentGraph.set(new SectionOcclusionGraph.GraphState(viewArea));
            this.invalidate();
        }
        else {
            this.currentGraph.set(null);
        }
    }

    @Environment(EnvType.CLIENT)
    record GraphEvents(LSet chunksWhichReceivedNeighbors, OQueue<SectionRenderDispatcher.RenderSection> sectionsToPropagateFrom) {

        GraphEvents() {
            this(new LHashSet(), new OArrayBlockingQueue<>());
        }
    }

    @Environment(EnvType.CLIENT)
    record GraphState(SectionOcclusionGraph.GraphStorage storage, SectionOcclusionGraph.GraphEvents events) {

        GraphState(ViewArea viewArea) {
            this(new SectionOcclusionGraph.GraphStorage(viewArea), new SectionOcclusionGraph.GraphEvents());
        }
    }

    @Environment(EnvType.CLIENT)
    static class GraphStorage {
        public final L2OMap<OList<SectionRenderDispatcher.RenderSection>> chunksWaitingForNeighbors;
        public final SectionOcclusionGraph.SectionToNodeMap sectionToNodeMap;
        public final Octree sectionTree;

        public GraphStorage(ViewArea viewArea) {
            this.sectionToNodeMap = new SectionOcclusionGraph.SectionToNodeMap(viewArea.sections.length);
            this.sectionTree = new Octree(viewArea.getCamSecX(), viewArea.getCamSecY(), viewArea.getCamSecZ(), viewArea.getRenderDistance(), viewArea.getHeight(), viewArea.level.getMinBuildHeight());
            this.chunksWaitingForNeighbors = new L2OHashMap<>();
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Node {
        byte directions;
        protected final SectionRenderDispatcher.RenderSection section;
        private byte sourceDirections;
        public final int step;

        Node(SectionRenderDispatcher.RenderSection renderSection, @Nullable Direction direction, int i) {
            this.section = renderSection;
            if (direction != null) {
                this.addSourceDirection(direction);
            }
            this.step = i;
        }

        void addSourceDirection(Direction direction) {
            this.sourceDirections |= (byte) (this.sourceDirections | 1 << direction.ordinal());
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Node node && this.section.getSectionPos() == node.section.getSectionPos();
        }

        boolean hasDirection(Direction direction) {
            return (this.directions & 1 << direction.ordinal()) > 0;
        }

        public boolean hasSourceDirection(Direction dir) {
            return (this.sourceDirections & 1 << dir.ordinal()) > 0;
        }

        boolean hasSourceDirections() {
            return this.sourceDirections != 0;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(this.section.getSectionPos());
        }

        void setDirections(byte b, Direction direction) {
            this.directions |= (byte) (b | 1 << direction.ordinal());
        }
    }

    @Environment(EnvType.CLIENT)
    static class SectionToNodeMap {
        private final SectionOcclusionGraph.Node[] nodes;

        SectionToNodeMap(int i) {
            this.nodes = new SectionOcclusionGraph.Node[i];
        }

        public @Nullable SectionOcclusionGraph.Node get(SectionRenderDispatcher.RenderSection renderSection) {
            int i = renderSection.index;
            return i >= 0 && i < this.nodes.length ? this.nodes[i] : null;
        }

        public void put(SectionRenderDispatcher.RenderSection renderSection, SectionOcclusionGraph.Node node) {
            this.nodes[renderSection.index] = node;
        }
    }
}
