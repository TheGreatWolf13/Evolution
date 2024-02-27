package tgw.evolution.world.lighting;

import it.unimi.dsi.fastutil.longs.LongIterator;
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
public final class SkyStarLightEngine extends StarLightEngine {

    private final int[] heightMapBlockChange = new int[16 * 16];
    private final boolean[] nullPropagationCheckCache;

    public SkyStarLightEngine(Level world) {
        super(true, world);
        Arrays.fill(this.heightMapBlockChange, Integer.MIN_VALUE); // clear heightmap
        this.nullPropagationCheckCache = new boolean[WorldUtil.getTotalLightSections(world)];
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
    protected void checkBlock(LightChunkGetter lightAccess, int worldX, int worldY, int worldZ) {
        // blocks can change opacity
        // blocks can change direction of propagation
        // same logic applies from BlockStarLightEngine#checkBlock
        int encodeOffset = this.coordinateOffset;
        int currentLevel = this.getLightLevel(worldX, worldY, worldZ);
        if (currentLevel == 15) {
            // must re-propagate clobbered source
            this.appendToIncreaseQueue(
                    worldX + ((long) worldZ << 6) + ((long) worldY << 6 + 6) + encodeOffset & (1L << 6 + 6 + 16) - 1
                    | (currentLevel & 0xFL) << 6 + 6 + 16
                    | (long) ALL_DIRECTIONS_BITSET << 6 + 6 + 16 + 4
                    | FLAG_HAS_SIDED_TRANSPARENT_BLOCKS // don't know if the block is conditionally transparent
            );
        }
        else {
            this.setLightLevel(worldX, worldY, worldZ, 0);
        }
        this.appendToDecreaseQueue(
                worldX + ((long) worldZ << 6) + ((long) worldY << 6 + 6) + encodeOffset & (1L << 6 + 6 + 16) - 1
                | (currentLevel & 0xFL) << 6 + 6 + 16
                | (long) ALL_DIRECTIONS_BITSET << 6 + 6 + 16 + 4
        );
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
                long propagateDirection = 1L << direction.ordinal(); // we only want to check in this direction
                for (int currY = highestNonEmptySection << 4, maxY = currY | 15; currY <= maxY; ++currY) {
                    for (int i = 0, currX = startX, currZ = startZ; i < 16; ++i, currX += incX, currZ += incZ) {
                        this.appendToIncreaseQueue(
                                currX + ((long) currZ << 6) + ((long) currY << 6 + 6) + encodeOffset & (1L << 6 + 6 + 16) - 1
                                | 15L << 6 + 6 + 16 // we know we're at full lit here
                                | propagateDirection << 6 + 6 + 16 + 4
                                // no transparent flag, we know for a fact there are no blocks here that could be directionally transparent (as the section is EMPTY)
                        );
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

    private void processDelayedDecreases() {
        // copied from performLightDecrease
        long[] queue = this.decreaseQueue;
        int decodeOffsetX = -this.encodeOffsetX;
        int decodeOffsetY = -this.encodeOffsetY;
        int decodeOffsetZ = -this.encodeOffsetZ;
        for (int i = 0, len = this.decreaseQueueInitialLength; i < len; ++i) {
            long queueValue = queue[i];
            int posX = ((int) queueValue & 63) + decodeOffsetX;
            int posZ = ((int) queueValue >>> 6 & 63) + decodeOffsetZ;
            int posY = ((int) queueValue >>> 12 & (1 << 16) - 1) + decodeOffsetY;
            this.setLightLevel(posX, posY, posZ, 0);
        }
    }

    private void processDelayedIncreases() {
        // copied from performLightIncrease
        long[] queue = this.increaseQueue;
        int decodeOffsetX = -this.encodeOffsetX;
        int decodeOffsetY = -this.encodeOffsetY;
        int decodeOffsetZ = -this.encodeOffsetZ;
        for (int i = 0, len = this.increaseQueueInitialLength; i < len; ++i) {
            long queueValue = queue[i];
            int posX = ((int) queueValue & 63) + decodeOffsetX;
            int posZ = ((int) queueValue >>> 6 & 63) + decodeOffsetZ;
            int posY = ((int) queueValue >>> 12 & (1 << 16) - 1) + decodeOffsetY;
            int propagatedLightLevel = (int) (queueValue >>> 6 + 6 + 16 & 0xF);
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
        for (LongIterator it = positions.iterator(); it.hasNext(); ) {
            long pos = it.nextLong();
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
        // nibbles are initialised, as we want to avoid clobbering nibble values so what when
        // below nibbles are initialised they aren't reading from partially modified nibbles
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
                long propagateDirection = AxisDirection.POSITIVE_Y.everythingButThisDirection;
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
                    this.appendToDecreaseQueue(
                            columnX + ((long) columnZ << 6) + ((long) currY << 6 + 6) + encodeOffset & (1L << 6 + 6 + 16) - 1
                            | 15L << 6 + 6 + 16
                            | propagateDirection << 6 + 6 + 16 + 4
                            // do not set transparent blocks for the same reason we don't in the checkBlock method
                    );
                }
            }
        }
        // delayed light sets are processed here, and must be processed before checkBlock as checkBlock reads
        // immediate light value
        this.processDelayedIncreases();
        this.processDelayedDecreases();
        for (LongIterator it = positions.iterator(); it.hasNext(); ) {
            long pos = it.nextLong();
            this.checkBlock(lightAccess, BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos));

        }
        this.performLightDecrease(lightAccess);
    }

    private void rewriteNibbleCacheForSkylight() {
        for (int index = 0, max = this.nibbleCache.length; index < max; ++index) {
            SWMRNibbleArray nibble = this.nibbleCache[index];
            if (nibble != null && nibble.isNullNibbleUpdating()) {
                // stop propagation in these areas
                this.nibbleCache[index] = null;
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

    private int tryPropagateSkylight(BlockGetter world, int worldX, int startY, int worldZ, boolean extrudeInitialised, boolean delayLightSet) {
        int encodeOffset = this.coordinateOffset;
        if (this.getLightLevelExtruded(worldX, startY + 1, worldZ) != 15) {
            return startY;
        }
        // ensure this section is always checked
        this.checkNullSection(worldX >> 4, startY >> 4, worldZ >> 4, extrudeInitialised);
        BlockState above = this.getBlockState(worldX, startY + 1, worldZ);
        for (long propagateDirection = AxisDirection.POSITIVE_Y.everythingButThisDirection; startY >= this.minLightSection << 4; --startY) {
            if ((startY & 15) == 15) {
                // ensure this section is always checked
                this.checkNullSection(worldX >> 4, startY >> 4, worldZ >> 4, extrudeInitialised);
            }
            BlockState current = this.getBlockState(worldX, startY, worldZ);
            VoxelShape fromShape;
            if (above.isConditionallyFullOpaque()) {
                fromShape = above.getFaceOcclusionShape_(world, worldX, startY + 1, worldZ, AxisDirection.NEGATIVE_Y.nms);
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
                this.appendToIncreaseQueue(
                        worldX + ((long) worldZ << 6) + ((long) startY << 6 + 6) + encodeOffset & (1L << 6 + 6 + 16) - 1
                        | 15L << 6 + 6 + 16 // we know we're at full lit here
                        | propagateDirection << 6 + 6 + 16 + 4
                );
            }
            else {
                long flags = 0L;
                if (current.isConditionallyFullOpaque()) {
                    VoxelShape cullingFace = current.getFaceOcclusionShape_(world, worldX, startY, worldZ, AxisDirection.POSITIVE_Y.nms);
                    if (Shapes.faceShapeOccludes(fromShape, cullingFace)) {
                        // can't propagate here, we're done on this column.
                        return startY;
                    }
                    flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
                }
                int opacity = current.getLightBlock_(world, worldX, startY, worldZ);
                if (opacity > 0) {
                    // let the queued value (if any) handle it from here.
                    return startY;
                }
                // light set delayed until we determine if this nibble section is null
                this.appendToIncreaseQueue(
                        worldX + ((long) worldZ << 6) + ((long) startY << 6 + 6) + encodeOffset & (1L << 6 + 6 + 16) - 1
                        | 15L << 6 + 6 + 16 // we know we're at full lit here
                        | propagateDirection << 6 + 6 + 16 + 4
                        | flags
                );
            }
            above = current;
            if (this.getNibbleFromCache(worldX >> 4, startY >> 4, worldZ >> 4) == null) {
                // we skip empty sections here, as this is just an easy way of making sure the above block
                // can propagate through air.
                // nothing can propagate in null sections, remove the queue entry for it
                --this.increaseQueueInitialLength;
                // advance currY to the top of the section below
                startY &= ~15;
                // note: this value ^ is actually 1 above the top, but the loop decrements by 1, so we actually
                // end up there
                // make sure this is marked as AIR
                above = AIR_BLOCK_STATE;
            }
            else if (!delayLightSet) {
                this.setLightLevel(worldX, startY, worldZ, 15);
            }
        }
        return startY;
    }
}
