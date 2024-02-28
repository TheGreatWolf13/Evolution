package tgw.evolution.world.lighting;

import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import net.minecraft.core.Direction;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.util.collection.sets.LSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class StarLightEngine<T extends SWMRArray> {

    protected static final BlockState AIR_BLOCK_STATE = Blocks.AIR.defaultBlockState();

    protected static final AxisDirection[] DIRECTIONS = AxisDirection.values();
    protected static final AxisDirection[] AXIS_DIRECTIONS = DIRECTIONS;
    protected static final AxisDirection[] ONLY_HORIZONTAL_DIRECTIONS = {
            AxisDirection.POSITIVE_X, AxisDirection.NEGATIVE_X,
            AxisDirection.POSITIVE_Z, AxisDirection.NEGATIVE_Z
    };

    protected static final long FLAG_WRITE_LEVEL = Long.MIN_VALUE >>> 2;
    protected static final long FLAG_RECHECK_LEVEL = Long.MIN_VALUE >>> 1;
    protected static final long FLAG_HAS_SIDED_TRANSPARENT_BLOCKS = Long.MIN_VALUE;
    protected static final AxisDirection[][] OLD_CHECK_DIRECTIONS = new AxisDirection[1 << 6][];
    protected static final int ALL_DIRECTIONS_BITSET = (1 << 6) - 1;

    static {
        for (int i = 0; i < OLD_CHECK_DIRECTIONS.length; ++i) {
            //noinspection ObjectAllocationInLoop
            List<AxisDirection> directions = new ArrayList<>();
            for (int bitset = i, len = Integer.bitCount(i), index = 0; index < len; ++index, bitset ^= -bitset & bitset) {
                directions.add(AXIS_DIRECTIONS[Integer.numberOfTrailingZeros(bitset)]);
            }
            //noinspection ObjectAllocationInLoop
            OLD_CHECK_DIRECTIONS[i] = directions.toArray(new AxisDirection[directions.size()]);
        }
    }

    protected final ChunkAccess[] chunkCache = new ChunkAccess[5 * 5];
    protected final int[] chunkCheckDelayedUpdatesCenter = new int[16 * 16];
    protected final int[] chunkCheckDelayedUpdatesNeighbour = new int[16 * 16];
    protected final int emittedLightMask;
    protected final boolean[][] emptinessMapCache = new boolean[5 * 5][];
    protected final boolean isClientSide;
    protected final Level level;
    protected final int maxLightSection;
    protected final int maxSection;
    protected final int minLightSection;
    protected final int minSection;
    protected final boolean[] notifyUpdateCache;
    protected final LevelChunkSection[] sectionCache;
    protected final boolean skylightPropagator;
    private final T[] nibbleCache;
    protected int chunkIndexOffset;
    protected int chunkOffsetX;
    protected int chunkOffsetY;
    protected int chunkOffsetZ;
    protected int chunkSectionIndexOffset;
    protected int coordinateOffset;
    protected long[] decreaseQueue = new long[16 * 16 * 16];
    protected int decreaseQueueInitialLength;
    protected int encodeOffsetX;
    protected int encodeOffsetY;
    protected int encodeOffsetZ;
    protected long[] increaseQueue = new long[16 * 16 * 16];
    protected int increaseQueueInitialLength;

    protected StarLightEngine(boolean skylightPropagator, Level level) {
        this.skylightPropagator = skylightPropagator;
        this.emittedLightMask = skylightPropagator ? 0 : 0x7FFF;
        this.isClientSide = level.isClientSide;
        this.level = level;
        this.minLightSection = WorldUtil.getMinLightSection(level);
        this.maxLightSection = WorldUtil.getMaxLightSection(level);
        this.minSection = WorldUtil.getMinSection(level);
        this.maxSection = WorldUtil.getMaxSection(level);
        this.sectionCache = new LevelChunkSection[5 * 5 * (this.maxLightSection - this.minLightSection + 1 + 2)]; // add two extra sections for buffer
        this.nibbleCache = (T[]) new SWMRArray[5 * 5 * (this.maxLightSection - this.minLightSection + 1 + 2)]; // add two extra sections for buffer
        this.notifyUpdateCache = new boolean[5 * 5 * (this.maxLightSection - this.minLightSection + 1 + 2)]; // add two extra sections for buffer
    }

    private static int branchlessAbs(int val) {
        // -n = -1 ^ n + 1
        int mask = val >> Integer.SIZE - 1; // -1 if < 0, 0 if >= 0
        return (mask ^ val) - mask; // if val < 0, then (0 ^ val) - 0 else (-1 ^ val) + 1
    }

    public static Boolean[] getEmptySectionsForChunk(ChunkAccess chunk) {
        final LevelChunkSection[] sections = chunk.getSections();
        final Boolean[] ret = new Boolean[sections.length];
        for (int i = 0; i < sections.length; ++i) {
            if (sections[i] == null || sections[i].hasOnlyAir()) {
                ret[i] = Boolean.TRUE;
            }
            else {
                ret[i] = Boolean.FALSE;
            }
        }
        return ret;
    }

    protected static SWMRNibbleArray[] getFilledEmptyLightNibble(int totalLightSections) {
        SWMRNibbleArray[] ret = new SWMRNibbleArray[totalLightSections];
        for (int i = 0, len = ret.length; i < len; ++i) {
            //noinspection ObjectAllocationInLoop
            ret[i] = new SWMRNibbleArray(null, true);
        }
        return ret;
    }

    public static SWMRNibbleArray[] getFilledEmptyLightNibble(LevelHeightAccessor world) {
        return getFilledEmptyLightNibble(WorldUtil.getTotalLightSections(world));
    }

    public static SWMRShortArray[] getFilledEmptyLightShort(LevelHeightAccessor world) {
        return getFilledEmptyLightShort(WorldUtil.getTotalLightSections(world));
    }

    protected static SWMRShortArray[] getFilledEmptyLightShort(int totalLightSections) {
        SWMRShortArray[] ret = new SWMRShortArray[totalLightSections];
        for (int i = 0, len = ret.length; i < len; ++i) {
            //noinspection ObjectAllocationInLoop
            ret[i] = new SWMRShortArray(null, true);
        }
        return ret;
    }

    protected final void appendToDecreaseQueue(long value) {
        int idx = this.decreaseQueueInitialLength++;
        long[] queue = this.decreaseQueue;
        if (idx >= queue.length) {
            queue = this.resizeDecreaseQueue();
        }
        queue[idx] = value;
    }

    protected final void appendToIncreaseQueue(final long value) {
        int idx = this.increaseQueueInitialLength++;
        long[] queue = this.increaseQueue;
        if (idx >= queue.length) {
            queue = this.resizeIncreaseQueue();
        }
        queue[idx] = value;
    }

    protected final int arrayLength() {
        return this.nibbleCache.length;
    }

    public final void blocksChangedInChunk(LightChunkGetter lightAccess, int chunkX, int chunkZ, LSet positions, Boolean @Nullable [] changedSections) {
        this.setupCaches(lightAccess, chunkX * 16 + 7, chunkZ * 16 + 7, true, true);
        try {
            ChunkAccess chunk = this.getChunkInCache(chunkX, chunkZ);
            if (chunk == null) {
                return;
            }
            if (changedSections != null) {
                boolean[] ret = this.handleEmptySectionChanges(lightAccess, chunk, changedSections, false);
                if (ret != null) {
                    this.setEmptinessMap(chunk, ret);
                }
            }
            if (!positions.isEmpty()) {
                this.propagateBlockChanges(lightAccess, chunk, positions);
            }
            this.updateVisible(lightAccess);
        }
        finally {
            this.destroyCaches();
        }
    }

    /**
     * if ret > expect, then the real value is at least ret (early returns if ret > expect, rather than calculating actual)
     * if ret == expect, then expect is the correct light value for pos
     * if ret < expect, then ret is the real light value
     */
    protected abstract int calculateLightValue(LightChunkGetter lightAccess, int worldX, int worldY, int worldZ, int expect);

    protected abstract boolean canUseChunk(ChunkAccess chunk);

    protected abstract void checkBlock(LightChunkGetter lightAccess, int worldX, int worldY, int worldZ);

    protected void checkChunkEdge(LightChunkGetter lightAccess, int chunkX, int chunkY, int chunkZ) {
        T currNibble = this.getNibbleFromCache(chunkX, chunkY, chunkZ);
        if (currNibble == null) {
            return;
        }
        for (AxisDirection direction : ONLY_HORIZONTAL_DIRECTIONS) {
            int neighbourOffX = direction.x;
            int neighbourOffZ = direction.z;
            T neighbourNibble = this.getNibbleFromCache(chunkX + neighbourOffX, chunkY, chunkZ + neighbourOffZ);
            if (neighbourNibble == null) {
                continue;
            }
            if (!currNibble.isInitialisedUpdating() && !neighbourNibble.isInitialisedUpdating()) {
                // both are zero, nothing to check.
                continue;
            }
            // this chunk
            int incX;
            int incZ;
            int startX;
            int startZ;
            if (neighbourOffX != 0) {
                // x direction
                incX = 0;
                incZ = 1;
                if (direction.x < 0) {
                    // negative
                    startX = chunkX << 4;
                }
                else {
                    startX = chunkX << 4 | 15;
                }
                startZ = chunkZ << 4;
            }
            else {
                // z direction
                incX = 1;
                incZ = 0;
                if (neighbourOffZ < 0) {
                    // negative
                    startZ = chunkZ << 4;
                }
                else {
                    startZ = chunkZ << 4 | 15;
                }
                startX = chunkX << 4;
            }
            int centerDelayedChecks = 0;
            int neighbourDelayedChecks = 0;
            for (int currY = chunkY << 4, maxY = currY | 15; currY <= maxY; ++currY) {
                for (int i = 0, currX = startX, currZ = startZ; i < 16; ++i, currX += incX, currZ += incZ) {
                    int neighbourX = currX + neighbourOffX;
                    int neighbourZ = currZ + neighbourOffZ;
                    int currentIndex = currX & 15 | (currZ & 15) << 4 | (currY & 15) << 8;
                    int currentLevel = currNibble.getUpdating(currentIndex);
                    int neighbourIndex = neighbourX & 15 | (neighbourZ & 15) << 4 | (currY & 15) << 8;
                    int neighbourLevel = neighbourNibble.getUpdating(neighbourIndex);
                    // the checks are delayed because the checkBlock method clobbers light values - which then
                    // affect later calculate light value operations. While they don't affect it in a behaviourly significant
                    // way, they do have a negative performance impact due to simply queueing more values
                    if (this.calculateLightValue(lightAccess, currX, currY, currZ, currentLevel) != currentLevel) {
                        this.chunkCheckDelayedUpdatesCenter[centerDelayedChecks++] = currentIndex;
                    }
                    if (this.calculateLightValue(lightAccess, neighbourX, currY, neighbourZ, neighbourLevel) != neighbourLevel) {
                        this.chunkCheckDelayedUpdatesNeighbour[neighbourDelayedChecks++] = neighbourIndex;
                    }
                }
            }
            int currentChunkOffX = chunkX << 4;
            int currentChunkOffZ = chunkZ << 4;
            int neighbourChunkOffX = chunkX + direction.x << 4;
            int neighbourChunkOffZ = chunkZ + direction.z << 4;
            int chunkOffY = chunkY << 4;
            for (int i = 0, len = Math.max(centerDelayedChecks, neighbourDelayedChecks); i < len; ++i) {
                // try to queue neighbouring data together
                // index = x | (z << 4) | (y << 8)
                if (i < centerDelayedChecks) {
                    int value = this.chunkCheckDelayedUpdatesCenter[i];
                    this.checkBlock(lightAccess, currentChunkOffX | value & 15, chunkOffY | value >>> 8, currentChunkOffZ | value >>> 4 & 15);
                }
                if (i < neighbourDelayedChecks) {
                    int value = this.chunkCheckDelayedUpdatesNeighbour[i];
                    this.checkBlock(lightAccess, neighbourChunkOffX | value & 15, chunkOffY | value >>> 8, neighbourChunkOffZ | value >>> 4 & 15);
                }
            }
        }
    }

    protected void checkChunkEdges(LightChunkGetter lightAccess, ChunkAccess chunk, ShortCollection sections) {
        ChunkPos chunkPos = chunk.getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        for (ShortIterator iterator = sections.iterator(); iterator.hasNext(); ) {
            this.checkChunkEdge(lightAccess, chunkX, iterator.nextShort(), chunkZ);
        }
        this.performLightDecrease(lightAccess);
    }

    /**
     * subclasses should not initialise caches, as this will always be done by the super call
     * subclasses should not invoke updateVisible, as this will always be done by the super call
     * verifies that light levels on these chunks edges are consistent with this chunk's neighbours
     * edges. if they are not, they are decreased (effectively performing the logic in checkBlock).
     * This does not resolve skylight source problems.
     */
    protected void checkChunkEdges(LightChunkGetter lightAccess, ChunkAccess chunk, int fromSection, int toSection) {
        ChunkPos chunkPos = chunk.getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        for (int currSectionY = toSection; currSectionY >= fromSection; --currSectionY) {
            this.checkChunkEdge(lightAccess, chunkX, currSectionY, chunkZ);
        }
        this.performLightDecrease(lightAccess);
    }

    public final void checkChunkEdges(LightChunkGetter lightAccess, int chunkX, int chunkZ) {
        this.setupCaches(lightAccess, chunkX * 16 + 7, chunkZ * 16 + 7, true, false);
        try {
            ChunkAccess chunk = this.getChunkInCache(chunkX, chunkZ);
            if (chunk == null) {
                return;
            }
            this.checkChunkEdges(lightAccess, chunk, this.minLightSection, this.maxLightSection);
            this.updateVisible(lightAccess);
        }
        finally {
            this.destroyCaches();
        }
    }

    public final void checkChunkEdges(LightChunkGetter lightAccess, int chunkX, int chunkZ, ShortCollection sections) {
        this.setupCaches(lightAccess, chunkX * 16 + 7, chunkZ * 16 + 7, true, false);
        try {
            ChunkAccess chunk = this.getChunkInCache(chunkX, chunkZ);
            if (chunk == null) {
                return;
            }
            this.checkChunkEdges(lightAccess, chunk, sections);
            this.updateVisible(lightAccess);
        }
        finally {
            this.destroyCaches();
        }
    }

    protected final void destroyCaches() {
        Arrays.fill(this.sectionCache, null);
        Arrays.fill(this.nibbleCache, null);
        Arrays.fill(this.chunkCache, null);
        Arrays.fill(this.emptinessMapCache, null);
        if (this.isClientSide) {
            Arrays.fill(this.notifyUpdateCache, false);
        }
    }

    public final void forceHandleEmptySectionChanges(LightChunkGetter lightAccess, ChunkAccess chunk, Boolean[] emptinessChanges) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        this.setupCaches(lightAccess, chunkX * 16 + 7, chunkZ * 16 + 7, true, true);
        try {
            // force current chunk into cache
            this.setChunkInCache(chunkX, chunkZ, chunk);
            this.setBlocksForChunkInCache(chunkX, chunkZ, chunk.getSections());
            this.setNibblesForChunkInCache(chunkX, chunkZ, this.getNibblesOnChunk(chunk));
            this.setEmptinessMapCache(chunkX, chunkZ, this.getEmptinessMap(chunk));
            boolean[] ret = this.handleEmptySectionChanges(lightAccess, chunk, emptinessChanges, false);
            if (ret != null) {
                this.setEmptinessMap(chunk, ret);
            }
            this.updateVisible(lightAccess);
        }
        finally {
            this.destroyCaches();
        }
    }

    protected final @Nullable T get(int index) {
        return this.nibbleCache[index];
    }

    protected final BlockState getBlockState(int worldX, int worldY, int worldZ) {
        final LevelChunkSection section = this.sectionCache[(worldX >> 4) + 5 * (worldZ >> 4) + 5 * 5 * (worldY >> 4) + this.chunkSectionIndexOffset];
        if (section != null) {
            return section.hasOnlyAir() ? AIR_BLOCK_STATE : section.getBlockState(worldX & 15, worldY & 15, worldZ & 15);
        }
        return AIR_BLOCK_STATE;
    }

    protected final BlockState getBlockState(final int sectionIndex, final int localIndex) {
        final LevelChunkSection section = this.sectionCache[sectionIndex];
        if (section != null) {
            return section.hasOnlyAir() ? AIR_BLOCK_STATE : section.states.get(localIndex);
        }
        return AIR_BLOCK_STATE;
    }

    protected final @Nullable ChunkAccess getChunkInCache(int chunkX, int chunkZ) {
        return this.chunkCache[chunkX + 5 * chunkZ + this.chunkIndexOffset];
    }

    protected final @Nullable LevelChunkSection getChunkSection(int chunkX, int chunkY, int chunkZ) {
        return this.sectionCache[chunkX + 5 * chunkZ + 5 * 5 * chunkY + this.chunkSectionIndexOffset];
    }

    protected final boolean @Nullable [] getEmptinessMap(int chunkX, int chunkZ) {
        return this.emptinessMapCache[chunkX + 5 * chunkZ + this.chunkIndexOffset];
    }

    protected abstract boolean @Nullable [] getEmptinessMap(ChunkAccess chunk);

    protected abstract T[] getFilledEmptyDataStructure(int totalLightSections);

    protected final int getLightLevel(int worldX, int worldY, int worldZ) {
        T nibble = this.nibbleCache[(worldX >> 4) + 5 * (worldZ >> 4) + 5 * 5 * (worldY >> 4) + this.chunkSectionIndexOffset];
        return nibble == null ? 0 : nibble.getUpdating(worldX & 15 | (worldZ & 15) << 4 | (worldY & 15) << 8);
    }

    protected final int getLightLevel(int sectionIndex, int localIndex) {
        T nibble = this.nibbleCache[sectionIndex];
        return nibble == null ? 0 : nibble.getUpdating(localIndex);
    }

    protected final @Nullable T getNibbleFromCache(int chunkX, int chunkY, int chunkZ) {
        return this.nibbleCache[chunkX + 5 * chunkZ + 5 * 5 * chunkY + this.chunkSectionIndexOffset];
    }

    protected abstract T[] getNibblesOnChunk(ChunkAccess chunk);

    /**
     * subclasses should not initialise caches, as this will always be done by the super call
     * subclasses should not invoke updateVisible, as this will always be done by the super call
     * subclasses are guaranteed that this is always called before a changed block set
     * newChunk specifies whether the changes describe a "first load" of a chunk or changes to existing, already loaded chunks
     * rets non-null when the emptiness map changed and needs to be updated
     */
    protected final boolean @Nullable [] handleEmptySectionChanges(LightChunkGetter lightAccess, ChunkAccess chunk, Boolean[] emptinessChanges, boolean unlit) {
        Level world = (Level) lightAccess.getLevel();
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        boolean[] chunkEmptinessMap = this.getEmptinessMap(chunkX, chunkZ);
        boolean[] ret = null;
        boolean needsInit = unlit || chunkEmptinessMap == null;
        if (needsInit) {
            this.setEmptinessMapCache(chunkX, chunkZ, ret = chunkEmptinessMap = new boolean[WorldUtil.getTotalSections(world)]);
        }
        // update emptiness map
        for (int sectionIndex = emptinessChanges.length - 1; sectionIndex >= 0; --sectionIndex) {
            Boolean valueBoxed = emptinessChanges[sectionIndex];
            if (valueBoxed == null) {
                if (!needsInit) {
                    continue;
                }
                LevelChunkSection section = this.getChunkSection(chunkX, sectionIndex + this.minSection, chunkZ);
                emptinessChanges[sectionIndex] = valueBoxed = section == null || section.hasOnlyAir() ? Boolean.TRUE : Boolean.FALSE;
            }
            chunkEmptinessMap[sectionIndex] = valueBoxed;
        }
        // now init neighbour nibbles
        for (int sectionIndex = emptinessChanges.length - 1; sectionIndex >= 0; --sectionIndex) {
            Boolean valueBoxed = emptinessChanges[sectionIndex];
            int sectionY = sectionIndex + this.minSection;
            if (valueBoxed == null) {
                continue;
            }
            boolean empty = valueBoxed;
            if (empty) {
                continue;
            }
            for (int dz = -1; dz <= 1; ++dz) {
                for (int dx = -1; dx <= 1; ++dx) {
                    // if we're not empty, we also need to initialise nibbles
                    // note: if we're unlit, we absolutely do not want to extrude, as light data isn't set up
                    final boolean extrude = (dx | dz) != 0 || !unlit;
                    for (int dy = 1; dy >= -1; --dy) {
                        this.initNibble(dx + chunkX, dy + sectionY, dz + chunkZ, extrude, false);
                    }
                }
            }
        }
        // check for de-init and lazy-init
        // lazy init is when chunks are being lit, so at the time they weren't loaded when their neighbours were running
        // init checks.
        for (int dz = -1; dz <= 1; ++dz) {
            for (int dx = -1; dx <= 1; ++dx) {
                // does this neighbour have 1 radius loaded?
                boolean neighboursLoaded = true;
                neighbour_loaded_search:
                for (int dz2 = -1; dz2 <= 1; ++dz2) {
                    for (int dx2 = -1; dx2 <= 1; ++dx2) {
                        if (this.getEmptinessMap(dx + dx2 + chunkX, dz + dz2 + chunkZ) == null) {
                            neighboursLoaded = false;
                            break neighbour_loaded_search;
                        }
                    }
                }
                for (int sectionY = this.maxLightSection; sectionY >= this.minLightSection; --sectionY) {
                    // check neighbours to see if we need to de-init this one
                    boolean allEmpty = true;
                    neighbour_search:
                    for (int dy2 = -1; dy2 <= 1; ++dy2) {
                        for (int dz2 = -1; dz2 <= 1; ++dz2) {
                            for (int dx2 = -1; dx2 <= 1; ++dx2) {
                                int y = sectionY + dy2;
                                if (y < this.minSection || y > this.maxSection) {
                                    // empty
                                    continue;
                                }
                                boolean[] emptinessMap = this.getEmptinessMap(dx + dx2 + chunkX, dz + dz2 + chunkZ);
                                if (emptinessMap != null) {
                                    if (!emptinessMap[y - this.minSection]) {
                                        allEmpty = false;
                                        break neighbour_search;
                                    }
                                }
                                else {
                                    LevelChunkSection section = this.getChunkSection(dx + dx2 + chunkX, y, dz + dz2 + chunkZ);
                                    if (section != null && !section.hasOnlyAir()) {
                                        allEmpty = false;
                                        break neighbour_search;
                                    }
                                }
                            }
                        }
                    }
                    if (allEmpty & neighboursLoaded) {
                        // can only de-init when neighbours are loaded
                        // de-init is fine to delay, as de-init is just an optimisation - it's not required for lighting
                        // to be correct
                        // all were empty, so de-init
                        this.setNibbleNull(dx + chunkX, sectionY, dz + chunkZ);
                    }
                    else if (!allEmpty) {
                        // must init
                        boolean extrude = (dx | dz) != 0 || !unlit;
                        this.initNibble(dx + chunkX, sectionY, dz + chunkZ, extrude, false);
                    }
                }
            }
        }
        return ret;
    }

    protected abstract void initNibble(int chunkX, int chunkY, int chunkZ, boolean extrude, boolean initRemovedNibbles);

    public final void light(LightChunkGetter lightAccess, ChunkAccess chunk, Boolean[] emptySections) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        this.setupCaches(lightAccess, chunkX * 16 + 7, chunkZ * 16 + 7, true, true);
        try {
            T[] nibbles = this.getFilledEmptyDataStructure(this.maxLightSection - this.minLightSection + 1);
            // force current chunk into cache
            this.setChunkInCache(chunkX, chunkZ, chunk);
            this.setBlocksForChunkInCache(chunkX, chunkZ, chunk.getSections());
            this.setNibblesForChunkInCache(chunkX, chunkZ, nibbles);
            this.setEmptinessMapCache(chunkX, chunkZ, this.getEmptinessMap(chunk));
            boolean[] ret = this.handleEmptySectionChanges(lightAccess, chunk, emptySections, true);
            if (ret != null) {
                this.setEmptinessMap(chunk, ret);
            }
            this.lightChunk(lightAccess, chunk, true);
            this.setNibbles(chunk, nibbles);
            this.updateVisible(lightAccess);
        }
        finally {
            this.destroyCaches();
        }
    }

    /**
     * subclasses should not initialise caches, as this will always be done by the super call
     * <p>
     * subclasses should not invoke updateVisible, as this will always be done by the super call
     * <p>
     * needsEdgeChecks applies when possibly loading vanilla data, which means we need to validate the current
     * chunks light values with respect to neighbours
     * <p>
     * subclasses should note that the emptiness changes are propagated BEFORE this is called, so this function
     * does not need to detect empty chunks itself (and it should do no handling for them either!)
     */
    protected abstract void lightChunk(LightChunkGetter lightAccess, ChunkAccess chunk, boolean needsEdgeChecks);

    protected void performLightDecrease(LightChunkGetter lightAccess) {
        BlockGetter level = lightAccess.getLevel();
        long[] queue = this.decreaseQueue;
        long[] increaseQueue = this.increaseQueue;
        int queueReadIndex = 0;
        int queueLength = this.decreaseQueueInitialLength;
        this.decreaseQueueInitialLength = 0;
        int increaseQueueLength = this.increaseQueueInitialLength;
        int decodeOffsetX = -this.encodeOffsetX;
        int decodeOffsetY = -this.encodeOffsetY;
        int decodeOffsetZ = -this.encodeOffsetZ;
        int encodeOffset = this.coordinateOffset;
        int sectionOffset = this.chunkSectionIndexOffset;
        int emittedMask = this.emittedLightMask;
        int printed = queueLength;
        Evolution.info("sky decrease queueLength = {}", printed);
        while (queueReadIndex < queueLength) {
            if (queueLength > printed) {
                printed = queueLength;
                Evolution.info("    I lied, it's actually = {}", printed);
            }
            long queueValue = queue[queueReadIndex++];
            int posX = ((int) queueValue & 63) + decodeOffsetX;
            int posZ = ((int) queueValue >>> 6 & 63) + decodeOffsetZ;
            int posY = ((int) queueValue >>> 12 & 65_535) + decodeOffsetY;
            int propagatedLightLevel = (int) (queueValue >>> 28 & 0x7FFF);
            AxisDirection[] checkDirections = OLD_CHECK_DIRECTIONS[(int) (queueValue >>> 43 & 63)];
            if ((queueValue & FLAG_HAS_SIDED_TRANSPARENT_BLOCKS) == 0L) {
                // we don't need to worry about our state here.
                for (AxisDirection propagate : checkDirections) {
                    int offX = posX + propagate.x;
                    int offY = posY + propagate.y;
                    int offZ = posZ + propagate.z;
                    int sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + 5 * 5 * (offY >> 4) + sectionOffset;
                    int localIndex = offX & 15 | (offZ & 15) << 4 | (offY & 15) << 8;
                    T currentNibble = this.nibbleCache[sectionIndex];
                    if (currentNibble == null) {
                        //Unloaded, nothing we can do
                        continue;
                    }
                    int currentLevel = currentNibble.getUpdating(localIndex);
                    if (currentLevel == 0) {
                        //Already at lowest, nothing to do
                        continue;
                    }
                    BlockState stateAt = this.getBlockState(sectionIndex, localIndex);
                    int opacityCached = stateAt.getOpacityIfCached();
                    if (opacityCached != -1) {
                        int targetLevel = Math.max(0, propagatedLightLevel - Math.max(1, opacityCached));
                        if (currentLevel > targetLevel) {
                            // it looks like another source propagated here, so re-propagate it
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] = offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                                                   | (currentLevel & 0x7FFFL) << 28
                                                                   | (long) ALL_DIRECTIONS_BITSET << 43
                                                                   | FLAG_RECHECK_LEVEL;
                            continue;
                        }
                        int emittedLight = stateAt.getLightEmission() & emittedMask;
                        if (emittedLight != 0) {
                            // re-propagate source
                            // note: do not set recheck level, or else the propagation will fail
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] = offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                                                   | (emittedLight & 0x7FFFL) << 28
                                                                   | (long) ALL_DIRECTIONS_BITSET << 43
                                                                   | (stateAt.isConditionallyFullOpaque() ? FLAG_WRITE_LEVEL | FLAG_HAS_SIDED_TRANSPARENT_BLOCKS : FLAG_WRITE_LEVEL);
                        }
                        currentNibble.set(localIndex, 0);
                        this.postLightUpdate(offX, offY, offZ);
                        if (targetLevel > 0) { // we actually need to propagate 0 just in case we find a neighbour...
                            if (queueLength >= queue.length) {
                                queue = this.resizeDecreaseQueue();
                            }
                            queue[queueLength++] =
                                    offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                    | (targetLevel & 0x7FFFL) << 28
                                    | propagate.everythingButTheOppositeDirection << 43;
                            continue;
                        }
                        continue;
                    }
                    //Not cached
                    long flags = 0;
                    if (stateAt.isConditionallyFullOpaque()) {
                        VoxelShape cullingFace = stateAt.getFaceOcclusionShape_(level, offX, offY, offZ, propagate.getOpposite().nms);
                        if (Shapes.faceShapeOccludes(Shapes.empty(), cullingFace)) {
                            continue;
                        }
                        flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
                    }
                    int opacity = stateAt.getLightBlock_(level, offX, offY, offZ);
                    int targetLevel = Math.max(0, propagatedLightLevel - Math.max(1, opacity));
                    long l = offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1;
                    if (currentLevel > targetLevel) {
                        // it looks like another source propagated here, so re-propagate it
                        if (increaseQueueLength >= increaseQueue.length) {
                            increaseQueue = this.resizeIncreaseQueue();
                        }
                        increaseQueue[increaseQueueLength++] =
                                l
                                | (currentLevel & 0x7FFFL) << 28
                                | (long) ALL_DIRECTIONS_BITSET << 43
                                | FLAG_RECHECK_LEVEL | flags;
                        continue;
                    }
                    int emittedLight = stateAt.getLightEmission() & emittedMask;
                    if (emittedLight != 0) {
                        // re-propagate source
                        // note: do not set recheck level, or else the propagation will fail
                        if (increaseQueueLength >= increaseQueue.length) {
                            increaseQueue = this.resizeIncreaseQueue();
                        }
                        increaseQueue[increaseQueueLength++] =
                                l
                                | (emittedLight & 0x7FFFL) << 28
                                | (long) ALL_DIRECTIONS_BITSET << 43
                                | flags | FLAG_WRITE_LEVEL;
                    }
                    currentNibble.set(localIndex, 0);
                    this.postLightUpdate(offX, offY, offZ);
                    if (targetLevel > 0) {
                        if (queueLength >= queue.length) {
                            queue = this.resizeDecreaseQueue();
                        }
                        queue[queueLength++] =
                                offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                | (targetLevel & 0x7FFFL) << 28
                                | propagate.everythingButTheOppositeDirection << 43
                                | flags;
                    }
                }
            }
            else {
                // we actually need to worry about our state here
                BlockState fromBlock = this.getBlockState(posX, posY, posZ);
                for (AxisDirection propagate : checkDirections) {
                    int offX = posX + propagate.x;
                    int offY = posY + propagate.y;
                    int offZ = posZ + propagate.z;
                    int sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + 5 * 5 * (offY >> 4) + sectionOffset;
                    int localIndex = offX & 15 | (offZ & 15) << 4 | (offY & 15) << 8;
                    VoxelShape fromShape = fromBlock.isConditionallyFullOpaque() ? fromBlock.getFaceOcclusionShape_(level, posX, posY, posZ, propagate.nms) : Shapes.empty();
                    if (fromShape != Shapes.empty() && Shapes.faceShapeOccludes(Shapes.empty(), fromShape)) {
                        continue;
                    }
                    T currentNibble = this.nibbleCache[sectionIndex];
                    if (currentNibble == null) {
                        //Unloaded, nothing we can do
                        continue;
                    }
                    int currentLevel = currentNibble.getUpdating(localIndex);
                    if (currentLevel == 0) {
                        // already at lowest, nothing to do
                        continue;
                    }
                    BlockState stateAt = this.getBlockState(sectionIndex, localIndex);
                    int opacityCached = stateAt.getOpacityIfCached();
                    if (opacityCached != -1) {
                        int targetLevel = Math.max(0, propagatedLightLevel - Math.max(1, opacityCached));
                        if (currentLevel > targetLevel) {
                            // it looks like another source propagated here, so re-propagate it
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                    | (currentLevel & 0x7FFFL) << 28
                                    | (long) ALL_DIRECTIONS_BITSET << 43
                                    | FLAG_RECHECK_LEVEL;
                            continue;
                        }
                        int emittedLight = stateAt.getLightEmission() & emittedMask;
                        if (emittedLight != 0) {
                            // re-propagate source
                            // note: do not set recheck level, or else the propagation will fail
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                    | (emittedLight & 0x7FFFL) << 28
                                    | (long) ALL_DIRECTIONS_BITSET << 43
                                    | (stateAt.isConditionallyFullOpaque() ? FLAG_WRITE_LEVEL | FLAG_HAS_SIDED_TRANSPARENT_BLOCKS : FLAG_WRITE_LEVEL);
                        }
                        currentNibble.set(localIndex, 0);
                        this.postLightUpdate(offX, offY, offZ);
                        if (targetLevel > 0) { // we actually need to propagate 0 just in case we find a neighbour...
                            if (queueLength >= queue.length) {
                                queue = this.resizeDecreaseQueue();
                            }
                            queue[queueLength++] =
                                    offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                    | (targetLevel & 0x7FFFL) << 28
                                    | propagate.everythingButTheOppositeDirection << 43;
                            continue;
                        }
                        continue;
                    }
                    //Not cached
                    long flags = 0;
                    if (stateAt.isConditionallyFullOpaque()) {
                        VoxelShape cullingFace = stateAt.getFaceOcclusionShape_(level, offX, offY, offZ, propagate.getOpposite().nms);
                        if (Shapes.faceShapeOccludes(fromShape, cullingFace)) {
                            continue;
                        }
                        flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
                    }
                    int opacity = stateAt.getLightBlock_(level, offX, offY, offZ);
                    int targetLevel = Math.max(0, propagatedLightLevel - Math.max(1, opacity));
                    if (currentLevel > targetLevel) {
                        // it looks like another source propagated here, so re-propagate it
                        if (increaseQueueLength >= increaseQueue.length) {
                            increaseQueue = this.resizeIncreaseQueue();
                        }
                        increaseQueue[increaseQueueLength++] =
                                offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                | (currentLevel & 0x7FFFL) << 28
                                | (long) ALL_DIRECTIONS_BITSET << 43
                                | FLAG_RECHECK_LEVEL | flags;
                        continue;
                    }
                    int emittedLight = stateAt.getLightEmission() & emittedMask;
                    if (emittedLight != 0) {
                        // re-propagate source
                        // note: do not set recheck level, or else the propagation will fail
                        if (increaseQueueLength >= increaseQueue.length) {
                            increaseQueue = this.resizeIncreaseQueue();
                        }
                        increaseQueue[increaseQueueLength++] =
                                offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                | (emittedLight & 0x7FFFL) << 28
                                | (long) ALL_DIRECTIONS_BITSET << 43
                                | flags | FLAG_WRITE_LEVEL;
                    }
                    currentNibble.set(localIndex, 0);
                    this.postLightUpdate(offX, offY, offZ);
                    if (targetLevel > 0) { // we actually need to propagate 0 just in case we find a neighbour...
                        if (queueLength >= queue.length) {
                            queue = this.resizeDecreaseQueue();
                        }
                        queue[queueLength++] =
                                offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                | (targetLevel & 0x7FFFL) << 28
                                | propagate.everythingButTheOppositeDirection << 43
                                | flags;
                    }
                }
            }
        }
        // propagate sources we clobbered
        this.increaseQueueInitialLength = increaseQueueLength;
        this.performLightIncrease(lightAccess);
    }

    protected void performLightIncrease(LightChunkGetter lightAccess) {
        BlockGetter world = lightAccess.getLevel();
        long[] queue = this.increaseQueue;
        int queueReadIndex = 0;
        int queueLength = this.increaseQueueInitialLength;
        this.increaseQueueInitialLength = 0;
        int decodeOffsetX = -this.encodeOffsetX;
        int decodeOffsetY = -this.encodeOffsetY;
        int decodeOffsetZ = -this.encodeOffsetZ;
        int encodeOffset = this.coordinateOffset;
        int sectionOffset = this.chunkSectionIndexOffset;
        int printed = queueLength;
        Evolution.info("sky increase queueLength = {}", printed);
        while (queueReadIndex < queueLength) {
            if (queueLength > printed) {
                printed = queueLength;
                Evolution.info("    I lied, it's actually = {}", printed);
            }
            long queueValue = queue[queueReadIndex++];
            int posX = ((int) queueValue & 63) + decodeOffsetX;
            int posZ = ((int) queueValue >>> 6 & 63) + decodeOffsetZ;
            int posY = ((int) queueValue >>> 12 & 65_535) + decodeOffsetY;
            int propagatedLightLevel = (int) (queueValue >>> 28 & 0x7FFFL);
            AxisDirection[] checkDirections = OLD_CHECK_DIRECTIONS[(int) (queueValue >>> 43 & 63L)];
            if ((queueValue & FLAG_RECHECK_LEVEL) != 0L) {
                if (this.getLightLevel(posX, posY, posZ) != propagatedLightLevel) {
                    // not at the level we expect, so something changed.
                    continue;
                }
            }
            else if ((queueValue & FLAG_WRITE_LEVEL) != 0L) {
                // these are used to restore block sources after a propagation decrease
                this.setLightLevel(posX, posY, posZ, propagatedLightLevel);
            }
            if ((queueValue & FLAG_HAS_SIDED_TRANSPARENT_BLOCKS) == 0L) {
                // we don't need to worry about our state here.
                for (AxisDirection propagate : checkDirections) {
                    int offX = posX + propagate.x;
                    int offY = posY + propagate.y;
                    int offZ = posZ + propagate.z;
                    int sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + 5 * 5 * (offY >> 4) + sectionOffset;
                    int localIndex = offX & 15 | (offZ & 15) << 4 | (offY & 15) << 8;
                    T currentNibble = this.nibbleCache[sectionIndex];
                    if (currentNibble == null) {
                        //Unloaded
                        continue;
                    }
                    int currentLevel = currentNibble.getUpdating(localIndex);
                    if (currentLevel >= propagatedLightLevel - 1) {
                        //Already at the level we want or more
                        continue;
                    }
                    BlockState stateAt = this.getBlockState(sectionIndex, localIndex);
                    int opacityCached = stateAt.getOpacityIfCached();
                    if (opacityCached != -1) {
                        //Cached
                        int targetLevel = propagatedLightLevel - Math.max(1, opacityCached);
                        if (targetLevel > currentLevel) {
                            currentNibble.set(localIndex, targetLevel);
                            this.postLightUpdate(offX, offY, offZ);
                            if (targetLevel > 1) {
                                if (queueLength >= queue.length) {
                                    queue = this.resizeIncreaseQueue();
                                }
                                queue[queueLength++] =
                                        offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                        | (targetLevel & 0x7FFFL) << 28
                                        | propagate.everythingButTheOppositeDirection << 43;
                                continue;
                            }
                        }
                        continue;
                    }
                    //Not cached
                    long flags = 0;
                    if (stateAt.isConditionallyFullOpaque()) {
                        VoxelShape cullingFace = stateAt.getFaceOcclusionShape_(world, offX, offY, offZ, propagate.getOpposite().nms);
                        if (Shapes.faceShapeOccludes(Shapes.empty(), cullingFace)) {
                            continue;
                        }
                        flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
                    }
                    int opacity = stateAt.getLightBlock_(world, offX, offY, offZ);
                    int targetLevel = propagatedLightLevel - Math.max(1, opacity);
                    if (targetLevel <= currentLevel) {
                        continue;
                    }
                    currentNibble.set(localIndex, targetLevel);
                    this.postLightUpdate(offX, offY, offZ);
                    if (targetLevel > 1) {
                        if (queueLength >= queue.length) {
                            queue = this.resizeIncreaseQueue();
                        }
                        queue[queueLength++] =
                                offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                | (targetLevel & 0x7FFFL) << 28
                                | propagate.everythingButTheOppositeDirection << 43
                                | flags;
                    }
                }
            }
            else {
                // we actually need to worry about our state here
                BlockState fromBlock = this.getBlockState(posX, posY, posZ);
                for (AxisDirection propagate : checkDirections) {
                    int offX = posX + propagate.x;
                    int offY = posY + propagate.y;
                    int offZ = posZ + propagate.z;
                    VoxelShape fromShape = fromBlock.isConditionallyFullOpaque() ? fromBlock.getFaceOcclusionShape_(world, posX, posY, posZ, propagate.nms) : Shapes.empty();
                    if (fromShape != Shapes.empty() && Shapes.faceShapeOccludes(Shapes.empty(), fromShape)) {
                        continue;
                    }
                    int sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + 5 * 5 * (offY >> 4) + sectionOffset;
                    int localIndex = offX & 15 | (offZ & 15) << 4 | (offY & 15) << 8;
                    T currentNibble = this.nibbleCache[sectionIndex];
                    if (currentNibble == null) {
                        //Unloaded
                        continue;
                    }
                    int currentLevel = currentNibble.getUpdating(localIndex);
                    if (currentLevel >= propagatedLightLevel - 1) {
                        //Already at the level we want or more
                        continue;
                    }
                    BlockState stateAt = this.getBlockState(sectionIndex, localIndex);
                    int opacityCached = stateAt.getOpacityIfCached();
                    if (opacityCached != -1) {
                        //Cached
                        final int targetLevel = propagatedLightLevel - Math.max(1, opacityCached);
                        if (targetLevel > currentLevel) {
                            currentNibble.set(localIndex, targetLevel);
                            this.postLightUpdate(offX, offY, offZ);
                            if (targetLevel > 1) {
                                if (queueLength >= queue.length) {
                                    queue = this.resizeIncreaseQueue();
                                }
                                queue[queueLength++] =
                                        offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                        | (targetLevel & 0x7FFFL) << 28
                                        | propagate.everythingButTheOppositeDirection << 43;
                                continue;
                            }
                        }
                        continue;
                    }
                    //Not cached
                    long flags = 0;
                    if (stateAt.isConditionallyFullOpaque()) {
                        VoxelShape cullingFace = stateAt.getFaceOcclusionShape_(world, offX, offY, offZ, propagate.getOpposite().nms);
                        if (Shapes.faceShapeOccludes(fromShape, cullingFace)) {
                            continue;
                        }
                        flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
                    }
                    int opacity = stateAt.getLightBlock_(world, offX, offY, offZ);
                    int targetLevel = propagatedLightLevel - Math.max(1, opacity);
                    if (targetLevel <= currentLevel) {
                        continue;
                    }
                    currentNibble.set(localIndex, targetLevel);
                    this.postLightUpdate(offX, offY, offZ);
                    if (targetLevel > 1) {
                        if (queueLength >= queue.length) {
                            queue = this.resizeIncreaseQueue();
                        }
                        queue[queueLength++] =
                                offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                | (targetLevel & 0x7FFFL) << 28
                                | propagate.everythingButTheOppositeDirection << 43
                                | flags;
                    }
                }
            }
        }
    }

    protected final void postLightUpdate(int worldX, int worldY, int worldZ) {
        if (this.isClientSide) {
            int cx1 = worldX - 1 >> 4;
            int cx2 = worldX + 1 >> 4;
            int cy1 = worldY - 1 >> 4;
            int cy2 = worldY + 1 >> 4;
            int cz1 = worldZ - 1 >> 4;
            int cz2 = worldZ + 1 >> 4;
            for (int x = cx1; x <= cx2; ++x) {
                for (int y = cy1; y <= cy2; ++y) {
                    for (int z = cz1; z <= cz2; ++z) {
                        this.notifyUpdateCache[x + 5 * z + 5 * 5 * y + this.chunkSectionIndexOffset] = true;
                    }
                }
            }
        }
    }

    /**
     * subclasses should not initialise caches, as this will always be done by the super call
     * subclasses should not invoke updateVisible, as this will always be done by the super call
     */
    protected abstract void propagateBlockChanges(LightChunkGetter lightAccess, ChunkAccess atChunk, LSet positions);

    /**
     * pulls light from neighbours, and adds them into the increase queue. does not actually propagate.
     */
    protected final void propagateNeighbourLevels(ChunkAccess chunk, int fromSection, int toSection) {
        ChunkPos chunkPos = chunk.getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        for (int currSectionY = toSection; currSectionY >= fromSection; --currSectionY) {
            T currNibble = this.getNibbleFromCache(chunkX, currSectionY, chunkZ);
            if (currNibble == null) {
                continue;
            }
            for (AxisDirection direction : ONLY_HORIZONTAL_DIRECTIONS) {
                int neighbourOffX = direction.x;
                int neighbourOffZ = direction.z;
                T neighbourNibble = this.getNibbleFromCache(chunkX + neighbourOffX, currSectionY, chunkZ + neighbourOffZ);
                if (neighbourNibble == null || !neighbourNibble.isInitialisedUpdating()) {
                    // can't pull from 0
                    continue;
                }
                // neighbour chunk
                int incX;
                int incZ;
                int startX;
                int startZ;
                if (neighbourOffX != 0) {
                    // x direction
                    incX = 0;
                    incZ = 1;
                    if (direction.x < 0) {
                        // negative
                        startX = (chunkX << 4) - 1;
                    }
                    else {
                        startX = (chunkX << 4) + 16;
                    }
                    startZ = chunkZ << 4;
                }
                else {
                    // z direction
                    incX = 1;
                    incZ = 0;
                    if (neighbourOffZ < 0) {
                        // negative
                        startZ = (chunkZ << 4) - 1;
                    }
                    else {
                        startZ = (chunkZ << 4) + 16;
                    }
                    startX = chunkX << 4;
                }
                long propagateDirection = 1L << direction.getOpposite().ordinal(); // we only want to check in this direction towards this chunk
                int encodeOffset = this.coordinateOffset;
                for (int currY = currSectionY << 4, maxY = currY | 15; currY <= maxY; ++currY) {
                    for (int i = 0, currX = startX, currZ = startZ; i < 16; ++i, currX += incX, currZ += incZ) {
                        int level = neighbourNibble.getUpdating(currX & 15 | (currZ & 15) << 4 | (currY & 15) << 8);
                        if (level <= 1) {
                            // nothing to propagate
                            continue;
                        }
                        this.appendToIncreaseQueue(
                                currX + ((long) currZ << 6) + ((long) currY << 12) + encodeOffset & (1L << 28) - 1
                                | (level & 0x7FFFL) << 28
                                | propagateDirection << 43
                                | FLAG_HAS_SIDED_TRANSPARENT_BLOCKS // don't know if the current block is transparent, must check.
                        );
                    }
                }
            }
        }
    }

    protected final long[] resizeDecreaseQueue() {
        return this.decreaseQueue = Arrays.copyOf(this.decreaseQueue, this.decreaseQueue.length * 2);
    }

    protected final long[] resizeIncreaseQueue() {
        return this.increaseQueue = Arrays.copyOf(this.increaseQueue, this.increaseQueue.length * 2);
    }

    protected final void set(int index, @Nullable T data) {
        this.nibbleCache[index] = data;
    }

    protected final void setBlocksForChunkInCache(int chunkX, int chunkZ, LevelChunkSection[] sections) {
        for (int cy = this.minLightSection; cy <= this.maxLightSection; ++cy) {
            this.setChunkSectionInCache(chunkX, cy, chunkZ, cy >= this.minSection && cy <= this.maxSection ? sections[cy - this.minSection] : null);
        }
    }

    protected final void setChunkInCache(int chunkX, int chunkZ, ChunkAccess chunk) {
        this.chunkCache[chunkX + 5 * chunkZ + this.chunkIndexOffset] = chunk;
    }

    protected final void setChunkSectionInCache(int chunkX, int chunkY, int chunkZ, @Nullable LevelChunkSection section) {
        this.sectionCache[chunkX + 5 * chunkZ + 5 * 5 * chunkY + this.chunkSectionIndexOffset] = section;
    }

    protected abstract void setEmptinessMap(ChunkAccess chunk, boolean[] to);

    protected final void setEmptinessMapCache(int chunkX, int chunkZ, boolean @Nullable [] emptinessMap) {
        this.emptinessMapCache[chunkX + 5 * chunkZ + this.chunkIndexOffset] = emptinessMap;
    }

    protected final void setLightLevel(int worldX, int worldY, int worldZ, int level) {
        int sectionIndex = (worldX >> 4) + 5 * (worldZ >> 4) + 5 * 5 * (worldY >> 4) + this.chunkSectionIndexOffset;
        T nibble = this.nibbleCache[sectionIndex];
        if (nibble != null) {
            nibble.set(worldX & 15 | (worldZ & 15) << 4 | (worldY & 15) << 8, level);
            if (this.isClientSide) {
                int cx1 = worldX - 1 >> 4;
                int cx2 = worldX + 1 >> 4;
                int cy1 = worldY - 1 >> 4;
                int cy2 = worldY + 1 >> 4;
                int cz1 = worldZ - 1 >> 4;
                int cz2 = worldZ + 1 >> 4;
                for (int x = cx1; x <= cx2; ++x) {
                    for (int y = cy1; y <= cy2; ++y) {
                        for (int z = cz1; z <= cz2; ++z) {
                            this.notifyUpdateCache[x + 5 * z + 5 * 5 * y + this.chunkSectionIndexOffset] = true;
                        }
                    }
                }
            }
        }
    }

    protected final void setNibbleInCache(int chunkX, int chunkY, int chunkZ, T nibble) {
        this.nibbleCache[chunkX + 5 * chunkZ + 5 * 5 * chunkY + this.chunkSectionIndexOffset] = nibble;
    }

    protected abstract void setNibbleNull(int chunkX, int chunkY, int chunkZ);

    protected abstract void setNibbles(ChunkAccess chunk, T[] to);

    protected final void setNibblesForChunkInCache(int chunkX, int chunkZ, T[] nibbles) {
        for (int cy = this.minLightSection; cy <= this.maxLightSection; ++cy) {
            this.setNibbleInCache(chunkX, cy, chunkZ, nibbles[cy - this.minLightSection]);
        }
    }

    protected final void setupCaches(LightChunkGetter chunkProvider, int centerX, int centerZ, boolean relaxed, boolean tryToLoadChunksFor2Radius) {
        int centerChunkX = centerX >> 4;
        int centerChunkZ = centerZ >> 4;
        this.setupEncodeOffset(centerChunkX * 16 + 7, centerChunkZ * 16 + 7);
        int radius = tryToLoadChunksFor2Radius ? 2 : 1;
        for (int dz = -radius; dz <= radius; ++dz) {
            for (int dx = -radius; dx <= radius; ++dx) {
                int cx = centerChunkX + dx;
                int cz = centerChunkZ + dz;
                boolean isTwoRadius = Math.max(branchlessAbs(dx), branchlessAbs(dz)) == 2;
                ChunkAccess chunk = (ChunkAccess) chunkProvider.getChunkForLighting(cx, cz);
                if (chunk == null) {
                    if (relaxed | isTwoRadius) {
                        continue;
                    }
                    throw new IllegalArgumentException("Trying to propagate light update before 1 radius neighbours ready");
                }
                if (!this.canUseChunk(chunk)) {
                    continue;
                }
                this.setChunkInCache(cx, cz, chunk);
                this.setEmptinessMapCache(cx, cz, this.getEmptinessMap(chunk));
                if (!isTwoRadius) {
                    this.setBlocksForChunkInCache(cx, cz, chunk.getSections());
                    this.setNibblesForChunkInCache(cx, cz, this.getNibblesOnChunk(chunk));
                }
            }
        }
    }

    protected final void setupEncodeOffset(int centerX, int centerZ) {
        // 31 = center + encodeOffset
        this.encodeOffsetX = 31 - centerX;
        this.encodeOffsetY = -(this.minLightSection - 1) << 4; // we want 0 to be the smallest encoded value
        this.encodeOffsetZ = 31 - centerZ;
        // coordinateIndex = x | (z << 6) | (y << 12)
        this.coordinateOffset = this.encodeOffsetX + (this.encodeOffsetZ << 6) + (this.encodeOffsetY << 12);
        // 2 = (centerX >> 4) + chunkOffset
        this.chunkOffsetX = 2 - (centerX >> 4);
        this.chunkOffsetY = -(this.minLightSection - 1); // lowest should be 0
        this.chunkOffsetZ = 2 - (centerZ >> 4);
        // chunk index = x + (5 * z)
        this.chunkIndexOffset = this.chunkOffsetX + 5 * this.chunkOffsetZ;
        // chunk section index = x + (5 * z) + ((5*5) * y)
        this.chunkSectionIndexOffset = this.chunkIndexOffset + 5 * 5 * this.chunkOffsetY;
    }

    protected final void updateVisible(LightChunkGetter lightAccess) {
        for (int index = 0, max = this.nibbleCache.length; index < max; ++index) {
            T nibble = this.nibbleCache[index];
            if (!this.notifyUpdateCache[index] && (nibble == null || !nibble.isDirty())) {
                continue;
            }
            int chunkX = index % 5 - this.chunkOffsetX;
            int chunkZ = index / 5 % 5 - this.chunkOffsetZ;
            int ySections = this.maxSection - this.minSection + 1;
            int chunkY = index / (5 * 5) % (ySections + 2 + 2) - this.chunkOffsetY;
            if (nibble != null && nibble.updateVisible() || this.notifyUpdateCache[index]) {
                lightAccess.onLightUpdate_(this.skylightPropagator ? LightLayer.SKY : LightLayer.BLOCK, chunkX, chunkY, chunkZ);
            }
        }
    }

    protected enum AxisDirection {

        // Declaration order is important and relied upon. Do not change without modifying propagation code.
        POSITIVE_X(1, 0, 0),
        NEGATIVE_X(-1, 0, 0),
        POSITIVE_Z(0, 0, 1),
        NEGATIVE_Z(0, 0, -1),
        POSITIVE_Y(0, 1, 0),
        NEGATIVE_Y(0, -1, 0);

        static {
            POSITIVE_X.opposite = NEGATIVE_X;
            NEGATIVE_X.opposite = POSITIVE_X;
            POSITIVE_Z.opposite = NEGATIVE_Z;
            NEGATIVE_Z.opposite = POSITIVE_Z;
            POSITIVE_Y.opposite = NEGATIVE_Y;
            NEGATIVE_Y.opposite = POSITIVE_Y;
        }

        public final long everythingButTheOppositeDirection;
        public final long everythingButThisDirection;
        public final Direction nms;
        public final int x;
        public final int y;
        public final int z;
        private AxisDirection opposite;

        AxisDirection(final int x, final int y, final int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            //noinspection DataFlowIssue
            this.nms = Direction.fromNormal(x, y, z);
            this.everythingButThisDirection = ALL_DIRECTIONS_BITSET ^ 1 << this.ordinal();
            // positive is always even, negative is always odd. Flip the 1 bit to get the negative direction.
            this.everythingButTheOppositeDirection = ALL_DIRECTIONS_BITSET ^ 1 << (this.ordinal() ^ 1);
        }

        public AxisDirection getOpposite() {
            return this.opposite;
        }
    }
}
