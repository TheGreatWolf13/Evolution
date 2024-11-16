package tgw.evolution.world.lighting;

import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.sets.LSet;

import java.util.Arrays;

/**
 * Specification for managing the initialisation and de-initialisation of skylight nibble arrays:
 * <p>
 * Skylight nibble initialisation requires that non-empty chunk sections have 1 radius nibbles non-null.
 * <p>
 * This presents some problems, as vanilla is only guaranteed to have 0 radius neighbours loaded when editing blocks.
 * However, starlight fixes this so that it has 1 radius loaded. Still, we don't actually have guarantees
 * that we have the necessary chunks loaded to de-initialise neighbour sections (but we do have enough to de-initialise
 * our own) - we need a radius of 2 to de-initialise neighbour nibbles.
 * How do we solve this?
 * <p>
 * Each chunk will store the last known "emptiness" of sections for each of their 1 radius neighbour chunk sections.
 * If the chunk does not have full data, then its nibbles are NOT de-initialised. This is because obviously the
 * chunk did not go through the light stage yet - or its neighbours are not lit. In either case, once the last
 * known "emptiness" of neighbouring sections is filled with data, the chunk will run a full check of the data
 * to see if any of its nibbles need to be de-initialised.
 * <p>
 * The emptiness map allows us to de-initialise neighbour nibbles if the neighbour has it filled with data,
 * and if it doesn't have data then we know it will correctly de-initialise once it fills up.
 * <p>
 * Unlike vanilla, we store whether nibbles are uninitialised on disk - so we don't need any dumb hacking
 * around those.
 */
public final class SkyStarLightEngine extends StarLightEngine<SWMRNibbleArray> {

    private long[] decrQueue = new long[16];
    private int decrQueueLen;
    private final int[] heightMapBlockChange = new int[16 * 16];
    private long[] incrQueue = new long[16];
    private int incrQueueLen;
    private final boolean[] nullPropagationCheckCache;

    public SkyStarLightEngine(Level world) {
        super(true, world);
        Arrays.fill(this.heightMapBlockChange, Integer.MIN_VALUE); // clear heightmap
        this.nullPropagationCheckCache = new boolean[WorldUtil.getTotalLightSections(world)];
    }

    private void appendToDecreaseQueue(int x, int y, int z, int encodeOffset, int light, int directions, long flags) {
        int index = this.decrQueueLen++;
        long[] queue = this.decrQueue;
        if (index >= queue.length) {
            queue = this.resizeDecreaseQueue();
        }
        queue[index] = encodeToQueue(x, y, z, encodeOffset, light, directions, flags);
    }

    @Override
    protected void appendToIncreaseQueue(int x, int y, int z, int encodeOffset, int light, int directions, long flags) {
        int index = this.incrQueueLen++;
        long[] queue = this.incrQueue;
        if (index >= queue.length) {
            queue = this.resizeIncreaseQueue();
        }
        queue[index] = encodeToQueue(x, y, z, encodeOffset, light, directions, flags);
    }

    @Override
    protected int calculateLightValue(LightChunkGetter lightAccess, int worldX, int worldY, int worldZ, int expect) {
        if (expect == 15) {
            return expect;
        }
        int sectionOffset = this.chunkSectionIndexOffset;
        BlockState centerState = this.getBlockState(worldX, worldY, worldZ);
        int opacity = centerState.getOpacityIfCached();
        BlockState conditionallyOpaqueState;
        if (opacity < 0) {
            opacity = Math.max(1, centerState.getLightBlock_(lightAccess.getLevel(), worldX, worldY, worldZ));
            if (centerState.isConditionallyFullOpaque()) {
                conditionallyOpaqueState = centerState;
            }
            else {
                conditionallyOpaqueState = null;
            }
        }
        else {
            conditionallyOpaqueState = null;
            opacity = Math.max(1, opacity);
        }
        int level = 0;
        for (AxisDirection direction : AXIS_DIRECTIONS) {
            int offX = worldX + direction.x;
            int offY = worldY + direction.y;
            int offZ = worldZ + direction.z;
            int sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + 5 * 5 * (offY >> 4) + sectionOffset;
            int neighbourLevel = this.getLightLevel(sectionIndex, offX & 15 | (offZ & 15) << 4 | (offY & 15) << 8);
            if (neighbourLevel - 1 <= level) {
                // don't need to test transparency, we know it won't affect the result.
                continue;
            }
            BlockState neighbourState = this.getBlockState(offX, offY, offZ);
            if (neighbourState.isConditionallyFullOpaque()) {
                // here the block can be conditionally opaque (i.e. light cannot propagate from it), so we need to test that
                // we don't read the blockstate because most of the time this is false, so using the faster
                // known transparency lookup results in a net win
                VoxelShape neighbourFace = neighbourState.getFaceOcclusionShape_(lightAccess.getLevel(), offX, offY, offZ, direction.getOpposite().nms);
                VoxelShape thisFace = conditionallyOpaqueState == null ? Shapes.empty() : conditionallyOpaqueState.getFaceOcclusionShape_(lightAccess.getLevel(), worldX, worldY, worldZ, direction.nms);
                if (Shapes.faceShapeOccludes(thisFace, neighbourFace)) {
                    // not allowed to propagate
                    continue;
                }
            }
            int calculated = neighbourLevel - opacity;
            level = Math.max(calculated, level);
            if (level > expect) {
                return level;
            }
        }
        return level;
    }

    @Override
    protected boolean canUseChunk(ChunkAccess chunk) {
        // can only use chunks for sky stuff if their sections have been init'd
        return chunk.getStatus().isOrAfter(ChunkStatus.LIGHT) && (this.isClientSide || chunk.isLightCorrect());
    }

    @Override
    protected void checkBlock(LightChunkGetter lightAccess, int x, int y, int z) {
        // blocks can change opacity
        // blocks can change direction of propagation
        // same logic applies from BlockStarLightEngine#checkBlock
        int encodeOffset = this.coordinateOffset;
        int currentLevel = this.getLightLevel(x, y, z);
        if (currentLevel == 15) {
            // must re-propagate clobbered source
            // don't know if the block is conditionally transparent
            this.appendToIncreaseQueue(x, y, z, encodeOffset, currentLevel, ALL_DIRECTIONS_BITSET, FLAG_HAS_SIDED_TRANSPARENT_BLOCKS);
        }
        else {
            this.setLightLevel(x, y, z, 0);
        }
        this.appendToDecreaseQueue(x, y, z, encodeOffset, currentLevel, ALL_DIRECTIONS_BITSET, 0);
    }

    @Override
    protected void checkChunkEdges(LightChunkGetter lightAccess, ChunkAccess chunk, int fromSection, int toSection) {
        Arrays.fill(this.nullPropagationCheckCache, false);
        this.rewriteNibbleCacheForSkylight();
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        for (int y = toSection; y >= fromSection; --y) {
            this.checkNullSection(chunkX, y, chunkZ, true);
        }
        super.checkChunkEdges(lightAccess, chunk, fromSection, toSection);
    }

    @Override
    protected void checkChunkEdges(LightChunkGetter lightAccess, ChunkAccess chunk, ShortCollection sections) {
        Arrays.fill(this.nullPropagationCheckCache, false);
        this.rewriteNibbleCacheForSkylight();
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        for (ShortIterator iterator = sections.iterator(); iterator.hasNext(); ) {
            int y = iterator.nextShort();
            this.checkNullSection(chunkX, y, chunkZ, true);
        }
        super.checkChunkEdges(lightAccess, chunk, sections);
    }

    private void checkNullSection(int chunkX, int chunkY, int chunkZ, boolean extrudeInitialised) {
        // null chunk sections may have nibble neighbours in the horizontal 1 radius that are
        // non-null. Propagation to these neighbours is necessary.
        // What makes this easy is we know none of these neighbours are non-empty (otherwise
        // this nibble would be initialised). So, we don't have to initialise
        // the neighbours in the full 1 radius, because there's no worry that any "paths"
        // to the neighbours on this horizontal plane are blocked.
        if (chunkY < this.minLightSection || chunkY > this.maxLightSection || this.nullPropagationCheckCache[chunkY - this.minLightSection]) {
            return;
        }
        this.nullPropagationCheckCache[chunkY - this.minLightSection] = true;
        // check horizontal neighbours
        boolean needInitNeighbours = false;
        neighbour_search:
        for (int dz = -1; dz <= 1; ++dz) {
            for (int dx = -1; dx <= 1; ++dx) {
                final SWMRNibbleArray nibble = this.getNibbleFromCache(dx + chunkX, chunkY, dz + chunkZ);
                if (nibble != null && !nibble.isNullNibbleUpdating()) {
                    needInitNeighbours = true;
                    break neighbour_search;
                }
            }
        }
        if (needInitNeighbours) {
            for (int dz = -1; dz <= 1; ++dz) {
                for (int dx = -1; dx <= 1; ++dx) {
                    this.initNibble(dx + chunkX, chunkY, dz + chunkZ, (dx | dz) != 0 || extrudeInitialised, true);
                }
            }
        }
    }

    @Override
    protected boolean @Nullable [] getEmptinessMap(ChunkAccess chunk) {
        return chunk.getSkyEmptinessMap();
    }

    @Override
    protected SWMRNibbleArray[] getFilledEmptyDataStructure(int totalLightSections) {
        return getFilledEmptyLightNibble(totalLightSections);
    }

    private int getLightLevelExtruded(int worldX, int worldY, int worldZ) {
        int chunkX = worldX >> 4;
        int chunkY = worldY >> 4;
        int chunkZ = worldZ >> 4;
        SWMRNibbleArray nibble = this.getNibbleFromCache(chunkX, chunkY, chunkZ);
        if (nibble != null) {
            return nibble.getUpdating(worldX, worldY, worldZ);
        }
        while (true) {
            if (++chunkY > this.maxLightSection) {
                return 15;
            }
            nibble = this.getNibbleFromCache(chunkX, chunkY, chunkZ);
            if (nibble != null) {
                return nibble.getUpdating(worldX, 0, worldZ);
            }
        }
    }

    @Override
    protected SWMRNibbleArray[] getNibblesOnChunk(ChunkAccess chunk) {
        return chunk.getSkyNibbles();
    }

    @Override
    protected void initNibble(int chunkX, int chunkY, int chunkZ, boolean extrude, boolean initRemovedNibbles) {
        if (chunkY < this.minLightSection || chunkY > this.maxLightSection || this.getChunkInCache(chunkX, chunkZ) == null) {
            return;
        }
        SWMRNibbleArray nibble = this.getNibbleFromCache(chunkX, chunkY, chunkZ);
        if (nibble == null) {
            if (!initRemovedNibbles) {
                throw new IllegalStateException();
            }
            this.setNibbleInCache(chunkX, chunkY, chunkZ, nibble = new SWMRNibbleArray(null, true));
        }
        this.initNibble(nibble, chunkX, chunkY, chunkZ, extrude);
    }

    private void initNibble(SWMRNibbleArray currNibble, int chunkX, int chunkY, int chunkZ, boolean extrude) {
        if (!currNibble.isNullNibbleUpdating()) {
            // already initialised
            return;
        }
        boolean[] emptinessMap = this.getEmptinessMap(chunkX, chunkZ);
        // are we above this chunk's lowest empty section?
        int lowestY = this.minLightSection - 1;
        for (int currY = this.maxSection; currY >= this.minSection; --currY) {
            if (emptinessMap == null) {
                // cannot delay nibble init for lit chunks, as we need to init to propagate into them.
                LevelChunkSection current = this.getChunkSection(chunkX, currY, chunkZ);
                if (current == null || current.hasOnlyAir()) {
                    continue;
                }
            }
            else {
                if (emptinessMap[currY - this.minSection]) {
                    continue;
                }
            }
            // should always be full lit here
            lowestY = currY;
            break;
        }
        if (chunkY > lowestY) {
            // we need to set this one to full
            SWMRNibbleArray nibble = this.getNibbleFromCache(chunkX, chunkY, chunkZ);
            assert nibble != null;
            nibble.setNonNull();
            nibble.setFull();
            return;
        }
        if (extrude) {
            // this nibble is going to depend solely on the skylight data above it
            // find first non-null data above (there does exist one, as we just found it above)
            for (int currY = chunkY + 1; currY <= this.maxLightSection; ++currY) {
                SWMRNibbleArray nibble = this.getNibbleFromCache(chunkX, currY, chunkZ);
                if (nibble != null && !nibble.isNullNibbleUpdating()) {
                    currNibble.setNonNull();
                    currNibble.extrudeLower(nibble);
                    break;
                }
            }
        }
        else {
            currNibble.setNonNull();
        }
    }

    @Override
    protected void lightChunk(LightChunkGetter lightAccess, ChunkAccess chunk, boolean needsEdgeChecks) {
        this.rewriteNibbleCacheForSkylight();
        Arrays.fill(this.nullPropagationCheckCache, false);
        BlockGetter world = lightAccess.getLevel();
        ChunkPos chunkPos = chunk.getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        LevelChunkSection[] sections = chunk.getSections();
        int highestNonEmptySection = this.maxSection;
        while (highestNonEmptySection == this.minSection - 1 || sections[highestNonEmptySection - this.minSection] == null || sections[highestNonEmptySection - this.minSection].hasOnlyAir()) {
            this.checkNullSection(chunkX, highestNonEmptySection, chunkZ, false);
            // try to propagate FULL to neighbours
            // check neighbours to see if we need to propagate into them
            for (AxisDirection direction : ONLY_HORIZONTAL_DIRECTIONS) {
                int neighbourX = chunkX + direction.x;
                int neighbourZ = chunkZ + direction.z;
                SWMRNibbleArray neighbourNibble = this.getNibbleFromCache(neighbourX, highestNonEmptySection, neighbourZ);
                if (neighbourNibble == null) {
                    // unloaded neighbour
                    // most of the time we fall here
                    continue;
                }
                // it looks like we need to propagate into the neighbour
                int incX;
                int incZ;
                int startX;
                int startZ;
                if (direction.x != 0) {
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
                    if (direction.z < 0) {
                        // negative
                        startZ = chunkZ << 4;
                    }
                    else {
                        startZ = chunkZ << 4 | 15;
                    }
                    startX = chunkX << 4;
                }
                int encodeOffset = this.coordinateOffset;
                int propagateDirection = 1 << direction.ordinal(); // we only want to check in this direction
                for (int currY = highestNonEmptySection << 4, maxY = currY | 15; currY <= maxY; ++currY) {
                    for (int i = 0, currX = startX, currZ = startZ; i < 16; ++i, currX += incX, currZ += incZ) {
                        // we know we're at full light here
                        // no transparent flag, we know for a fact there are no blocks here that could be directionally transparent (as the section is EMPTY)
                        this.appendToIncreaseQueue(currX, currY, currZ, encodeOffset, 15, propagateDirection, 0);
                    }
                }
            }
            if (highestNonEmptySection-- == this.minSection - 1) {
                break;
            }
        }
        if (highestNonEmptySection >= this.minSection) {
            // fill out our other sources
            int minX = chunkPos.x << 4;
            int maxX = chunkPos.x << 4 | 15;
            int minZ = chunkPos.z << 4;
            int maxZ = chunkPos.z << 4 | 15;
            int startY = highestNonEmptySection << 4 | 15;
            for (int currZ = minZ; currZ <= maxZ; ++currZ) {
                for (int currX = minX; currX <= maxX; ++currX) {
                    this.tryPropagateSkylight(world, currX, startY + 1, currZ, false, false);
                }
            }
        } // else: apparently the chunk is empty
        if (needsEdgeChecks) {
            // not required to propagate here, but this will reduce the hit of the edge checks
            this.performLightIncrease(lightAccess);
            for (int y = highestNonEmptySection; y >= this.minLightSection; --y) {
                this.checkNullSection(chunkX, y, chunkZ, false);
            }
            // no need to rewrite the nibble cache again
            super.checkChunkEdges(lightAccess, chunk, this.minLightSection, highestNonEmptySection);
        }
        else {
            for (int y = highestNonEmptySection; y >= this.minLightSection; --y) {
                this.checkNullSection(chunkX, y, chunkZ, false);
            }
            this.propagateNeighbourLevels(chunk, this.minLightSection, highestNonEmptySection);
            this.performLightIncrease(lightAccess);
        }
    }

    @Override
    protected void performLightDecrease(LightChunkGetter lightAccess) {
        BlockGetter level = lightAccess.getLevel();
        long[] queue = this.decrQueue;
        long[] increaseQueue = this.incrQueue;
        int queueReadIndex = 0;
        int queueLength = this.decrQueueLen;
        this.decrQueueLen = 0;
        int increaseQueueLength = this.incrQueueLen;
        int decodeOffsetX = -this.encodeOffsetX;
        int decodeOffsetY = -this.encodeOffsetY;
        int decodeOffsetZ = -this.encodeOffsetZ;
        int encodeOffset = this.coordinateOffset;
        int sectionOffset = this.chunkSectionIndexOffset;
        int emittedMask = this.emittedLightMask;
        while (queueReadIndex < queueLength) {
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
                    SWMRNibbleArray currentNibble = this.get(sectionIndex);
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
                            increaseQueue[increaseQueueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, currentLevel, ALL_DIRECTIONS_BITSET, FLAG_RECHECK_LEVEL);
                            continue;
                        }
                        int emittedLight = stateAt.getLightEmission() & emittedMask;
                        if (emittedLight != 0) {
                            // re-propagate source
                            // note: do not set recheck level, or else the propagation will fail
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, emittedLight, ALL_DIRECTIONS_BITSET, stateAt.isConditionallyFullOpaque() ? FLAG_WRITE_LEVEL | FLAG_HAS_SIDED_TRANSPARENT_BLOCKS : FLAG_WRITE_LEVEL);
                        }
                        currentNibble.set(localIndex, 0);
                        this.postLightUpdate(offX, offY, offZ);
                        if (targetLevel > 0) { // we actually need to propagate 0 just in case we find a neighbour...
                            if (queueLength >= queue.length) {
                                queue = this.resizeDecreaseQueue();
                            }
                            queue[queueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLevel, propagate.everythingButTheOppositeDirection, 0);
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
                    if (currentLevel > targetLevel) {
                        // it looks like another source propagated here, so re-propagate it
                        if (increaseQueueLength >= increaseQueue.length) {
                            increaseQueue = this.resizeIncreaseQueue();
                        }
                        increaseQueue[increaseQueueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, currentLevel, ALL_DIRECTIONS_BITSET, FLAG_RECHECK_LEVEL | flags);
                        continue;
                    }
                    int emittedLight = stateAt.getLightEmission() & emittedMask;
                    if (emittedLight != 0) {
                        // re-propagate source
                        // note: do not set recheck level, or else the propagation will fail
                        if (increaseQueueLength >= increaseQueue.length) {
                            increaseQueue = this.resizeIncreaseQueue();
                        }
                        increaseQueue[increaseQueueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, emittedLight, ALL_DIRECTIONS_BITSET, flags | FLAG_WRITE_LEVEL);
                    }
                    currentNibble.set(localIndex, 0);
                    this.postLightUpdate(offX, offY, offZ);
                    if (targetLevel > 0) {
                        if (queueLength >= queue.length) {
                            queue = this.resizeDecreaseQueue();
                        }
                        queue[queueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLevel, propagate.everythingButTheOppositeDirection, flags);
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
                    SWMRNibbleArray currentNibble = this.get(sectionIndex);
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
                            increaseQueue[increaseQueueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, currentLevel, ALL_DIRECTIONS_BITSET, FLAG_RECHECK_LEVEL);
                            continue;
                        }
                        int emittedLight = stateAt.getLightEmission() & emittedMask;
                        if (emittedLight != 0) {
                            // re-propagate source
                            // note: do not set recheck level, or else the propagation will fail
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, emittedLight, ALL_DIRECTIONS_BITSET, stateAt.isConditionallyFullOpaque() ? FLAG_WRITE_LEVEL | FLAG_HAS_SIDED_TRANSPARENT_BLOCKS : FLAG_WRITE_LEVEL);
                        }
                        currentNibble.set(localIndex, 0);
                        this.postLightUpdate(offX, offY, offZ);
                        if (targetLevel > 0) { // we actually need to propagate 0 just in case we find a neighbour...
                            if (queueLength >= queue.length) {
                                queue = this.resizeDecreaseQueue();
                            }
                            queue[queueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLevel, propagate.everythingButTheOppositeDirection, 0);
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
                        increaseQueue[increaseQueueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, currentLevel, ALL_DIRECTIONS_BITSET, FLAG_RECHECK_LEVEL | flags);
                        continue;
                    }
                    int emittedLight = stateAt.getLightEmission() & emittedMask;
                    if (emittedLight != 0) {
                        // re-propagate source
                        // note: do not set recheck level, or else the propagation will fail
                        if (increaseQueueLength >= increaseQueue.length) {
                            increaseQueue = this.resizeIncreaseQueue();
                        }
                        increaseQueue[increaseQueueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, emittedLight, ALL_DIRECTIONS_BITSET, flags | FLAG_WRITE_LEVEL);
                    }
                    currentNibble.set(localIndex, 0);
                    this.postLightUpdate(offX, offY, offZ);
                    if (targetLevel > 0) { // we actually need to propagate 0 just in case we find a neighbour...
                        if (queueLength >= queue.length) {
                            queue = this.resizeDecreaseQueue();
                        }
                        queue[queueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLevel, propagate.everythingButTheOppositeDirection, flags);
                    }
                }
            }
        }
        // propagate sources we clobbered
        this.incrQueueLen = increaseQueueLength;
        this.performLightIncrease(lightAccess);
    }

    @Override
    protected void performLightIncrease(LightChunkGetter lightAccess) {
        BlockGetter world = lightAccess.getLevel();
        long[] queue = this.incrQueue;
        int queueReadIndex = 0;
        int queueLength = this.incrQueueLen;
        this.incrQueueLen = 0;
        int decodeOffsetX = -this.encodeOffsetX;
        int decodeOffsetY = -this.encodeOffsetY;
        int decodeOffsetZ = -this.encodeOffsetZ;
        int encodeOffset = this.coordinateOffset;
        int sectionOffset = this.chunkSectionIndexOffset;
        while (queueReadIndex < queueLength) {
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
                    SWMRNibbleArray currentNibble = this.get(sectionIndex);
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
                                queue[queueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLevel, propagate.everythingButTheOppositeDirection, 0);
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
                        queue[queueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLevel, propagate.everythingButTheOppositeDirection, flags);
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
                    SWMRNibbleArray currentNibble = this.get(sectionIndex);
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
                                queue[queueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLevel, propagate.everythingButTheOppositeDirection, 0);
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
                        queue[queueLength++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLevel, propagate.everythingButTheOppositeDirection, flags);
                    }
                }
            }
        }
    }

    private void processDelayedDecreases() {
        // copied from performLightDecrease
        long[] queue = this.decrQueue;
        int decodeOffsetX = -this.encodeOffsetX;
        int decodeOffsetY = -this.encodeOffsetY;
        int decodeOffsetZ = -this.encodeOffsetZ;
        for (int i = 0, len = this.decrQueueLen; i < len; ++i) {
            long queueValue = queue[i];
            int posX = ((int) queueValue & 63) + decodeOffsetX;
            int posZ = ((int) queueValue >>> 6 & 63) + decodeOffsetZ;
            int posY = ((int) queueValue >>> 12 & (1 << 16) - 1) + decodeOffsetY;
            this.setLightLevel(posX, posY, posZ, 0);
        }
    }

    private void processDelayedIncreases() {
        // copied from performLightIncrease
        long[] queue = this.incrQueue;
        int decodeOffsetX = -this.encodeOffsetX;
        int decodeOffsetY = -this.encodeOffsetY;
        int decodeOffsetZ = -this.encodeOffsetZ;
        for (int i = 0, len = this.incrQueueLen; i < len; ++i) {
            long queueValue = queue[i];
            int posX = ((int) queueValue & 63) + decodeOffsetX;
            int posZ = ((int) queueValue >>> 6 & 63) + decodeOffsetZ;
            int posY = ((int) queueValue >>> 12 & 65_535) + decodeOffsetY;
            int propagatedLightLevel = (int) (queueValue >>> 28 & 0xF);
            this.setLightLevel(posX, posY, posZ, propagatedLightLevel);
        }
    }

    @Override
    protected void propagateBlockChanges(LightChunkGetter lightAccess, ChunkAccess atChunk, LSet positions) {
        this.rewriteNibbleCacheForSkylight();
        Arrays.fill(this.nullPropagationCheckCache, false);
        BlockGetter world = lightAccess.getLevel();
        int chunkX = atChunk.getPos().x;
        int chunkZ = atChunk.getPos().z;
        int heightMapOffset = chunkX * -16 + chunkZ * -16 * 16;
        // setup heightmap for changes
        for (long it = positions.beginIteration(); positions.hasNextIteration(it); it = positions.nextEntry(it)) {
            long pos = positions.getIteration(it);
            int x = BlockPos.getX(pos);
            int y = BlockPos.getY(pos);
            int z = BlockPos.getZ(pos);
            int index = x + (z << 4) + heightMapOffset;
            int curr = this.heightMapBlockChange[index];
            if (y > curr) {
                this.heightMapBlockChange[index] = y;
            }
        }
        // note: light sets are delayed while processing skylight source changes due to how
        // nibbles are initialized, as we want to avoid clobbering nibble values so what when
        // below nibbles are initialized they aren't reading from partially modified nibbles
        // now we can recalculate the sources for the changed columns
        for (int index = 0; index < 16 * 16; ++index) {
            int maxY = this.heightMapBlockChange[index];
            if (maxY == Integer.MIN_VALUE) {
                // not changed
                continue;
            }
            this.heightMapBlockChange[index] = Integer.MIN_VALUE; // restore default for next caller
            int columnX = index & 15 | chunkX << 4;
            int columnZ = index >>> 4 | chunkZ << 4;
            // try and propagate from the above y
            // delay light set until after processing all sources to set up
            int maxPropagationY = this.tryPropagateSkylight(world, columnX, maxY, columnZ, true, true);
            // maxPropagationY is now the highest block that could not be propagated to
            // remove all sources below that are 15
            int encodeOffset = this.coordinateOffset;
            if (this.getLightLevelExtruded(columnX, maxPropagationY, columnZ) == 15) {
                // ensure section is checked
                this.checkNullSection(columnX >> 4, maxPropagationY >> 4, columnZ >> 4, true);
                int propagateDirection = AxisDirection.POSITIVE_Y.everythingButThisDirection;
                for (int currY = maxPropagationY; currY >= this.minLightSection << 4; --currY) {
                    if ((currY & 15) == 15) {
                        // ensure section is checked
                        this.checkNullSection(columnX >> 4, currY >> 4, columnZ >> 4, true);
                    }
                    // ensure section below is always checked
                    SWMRNibbleArray nibble = this.getNibbleFromCache(columnX >> 4, currY >> 4, columnZ >> 4);
                    if (nibble == null) {
                        // advance currY to the top of the section below
                        currY &= ~15;
                        // note: this value ^ is actually 1 above the top, but the loop decrements by 1, so we actually
                        // end up there
                        continue;
                    }
                    if (nibble.getUpdating(columnX, currY, columnZ) != 15) {
                        break;
                    }
                    // delay light set until after processing all sources to set up
                    // do not set transparent blocks for the same reason we don't in the checkBlock method
                    this.appendToDecreaseQueue(columnX, currY, columnZ, encodeOffset, 15, propagateDirection, 0);
                }
            }
        }
        // delayed light sets are processed here, and must be processed before checkBlock as checkBlock reads
        // immediate light value
        this.processDelayedIncreases();
        this.processDelayedDecreases();
        for (long it = positions.beginIteration(); positions.hasNextIteration(it); it = positions.nextEntry(it)) {
            long pos = positions.getIteration(it);
            this.checkBlock(lightAccess, BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos));
        }
        this.performLightDecrease(lightAccess);
    }

    private long[] resizeDecreaseQueue() {
        return this.decrQueue = Arrays.copyOf(this.decrQueue, this.decrQueue.length * 2);
    }

    private long[] resizeIncreaseQueue() {
        return this.incrQueue = Arrays.copyOf(this.incrQueue, this.incrQueue.length * 2);
    }

    private void rewriteNibbleCacheForSkylight() {
        for (int index = 0, max = this.arrayLength(); index < max; ++index) {
            SWMRNibbleArray nibble = this.get(index);
            if (nibble != null && nibble.isNullNibbleUpdating()) {
                // stop propagation in these areas
                this.set(index, null);
                nibble.updateVisible();
            }
        }
    }

    @Override
    protected void setEmptinessMap(ChunkAccess chunk, boolean[] to) {
        chunk.setSkyEmptinessMap(to);
    }

    @Override
    protected void setNibbleNull(int chunkX, int chunkY, int chunkZ) {
        final SWMRNibbleArray nibble = this.getNibbleFromCache(chunkX, chunkY, chunkZ);
        if (nibble != null) {
            nibble.setNull();
        }
    }

    @Override
    protected void setNibbles(ChunkAccess chunk, SWMRNibbleArray[] to) {
        chunk.setSkyNibbles(to);
    }

    private int tryPropagateSkylight(BlockGetter world, int x, int startY, int z, boolean extrudeInitialised, boolean delayLightSet) {
        int encodeOffset = this.coordinateOffset;
        if (this.getLightLevelExtruded(x, startY + 1, z) != 15) {
            return startY;
        }
        // ensure this section is always checked
        this.checkNullSection(x >> 4, startY >> 4, z >> 4, extrudeInitialised);
        BlockState above = this.getBlockState(x, startY + 1, z);
        for (int propagateDirection = AxisDirection.POSITIVE_Y.everythingButThisDirection; startY >= this.minLightSection << 4; --startY) {
            if ((startY & 15) == 15) {
                // ensure this section is always checked
                this.checkNullSection(x >> 4, startY >> 4, z >> 4, extrudeInitialised);
            }
            BlockState current = this.getBlockState(x, startY, z);
            VoxelShape fromShape;
            if (above.isConditionallyFullOpaque()) {
                fromShape = above.getFaceOcclusionShape_(world, x, startY + 1, z, AxisDirection.NEGATIVE_Y.nms);
                if (Shapes.faceShapeOccludes(Shapes.empty(), fromShape)) {
                    // above won't let us propagate
                    return startY;
                }
            }
            else {
                fromShape = Shapes.empty();
            }
            int opacityIfCached = current.getOpacityIfCached();
            // does light propagate from the top down?
            if (opacityIfCached != -1) {
                if (opacityIfCached != 0) {
                    // we cannot propagate 15 through this
                    return startY;
                }
                // most of the time it falls here.
                // add to propagate
                // light set delayed until we determine if this nibble section is null
                // we know we're at full light here
                this.appendToIncreaseQueue(x, startY, z, encodeOffset, 15, propagateDirection, 0);
            }
            else {
                long flags = 0L;
                if (current.isConditionallyFullOpaque()) {
                    VoxelShape cullingFace = current.getFaceOcclusionShape_(world, x, startY, z, AxisDirection.POSITIVE_Y.nms);
                    if (Shapes.faceShapeOccludes(fromShape, cullingFace)) {
                        // can't propagate here, we're done on this column.
                        return startY;
                    }
                    flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
                }
                int opacity = current.getLightBlock_(world, x, startY, z);
                if (opacity > 0) {
                    // let the queued value (if any) handle it from here.
                    return startY;
                }
                // light set delayed until we determine if this nibble section is null
                // we know we're at full light here
                this.appendToIncreaseQueue(x, startY, z, encodeOffset, 15, propagateDirection, flags);
            }
            above = current;
            if (this.getNibbleFromCache(x >> 4, startY >> 4, z >> 4) == null) {
                // we skip empty sections here, as this is just an easy way of making sure the above block
                // can propagate through air.
                // nothing can propagate in null sections, remove the queue entry for it
                --this.incrQueueLen;
                // advance currY to the top of the section below
                startY &= ~15;
                // note: this value ^ is actually 1 above the top, but the loop decrements by 1, so we actually
                // end up there
                // make sure this is marked as AIR
                above = AIR_BLOCK_STATE;
            }
            else if (!delayLightSet) {
                this.setLightLevel(x, startY, z, 15);
            }
        }
        return startY;
    }
}
