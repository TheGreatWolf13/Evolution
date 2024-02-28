package tgw.evolution.world.lighting;

import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.client.renderer.ambient.DynamicLights;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.collection.lists.LArrayList;
import tgw.evolution.util.collection.lists.LList;
import tgw.evolution.util.collection.sets.LSet;

public final class BlockStarLightEngine extends StarLightEngine<SWMRShortArray> {

    public BlockStarLightEngine(Level level) {
        super(false, level);
    }

    @Override
    protected int calculateLightValue(LightChunkGetter lightAccess, int worldX, int worldY, int worldZ, int expect) {
        BlockState centerState = this.getBlockState(worldX, worldY, worldZ);
        int level = centerState.getLightEmission() & 0x7FFF;
        if (level >= 15 - 1 || level > expect) {
            return level;
        }
        int sectionOffset = this.chunkSectionIndexOffset;
        BlockState conditionallyOpaqueState;
        int opacity = centerState.getOpacityIfCached();
        if (opacity == -1) {
            opacity = centerState.getLightBlock_(lightAccess.getLevel(), worldX, worldY, worldZ);
            if (centerState.isConditionallyFullOpaque()) {
                conditionallyOpaqueState = centerState;
            }
            else {
                conditionallyOpaqueState = null;
            }
        }
        else if (opacity >= 15) {
            return level;
        }
        else {
            conditionallyOpaqueState = null;
        }
        opacity = Math.max(1, opacity);
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
            // passed transparency,
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
        return chunk.getStatus().isOrAfter(ChunkStatus.LIGHT) && (this.isClientSide || chunk.isLightCorrect());
    }

    @Override
    protected void checkBlock(LightChunkGetter lightAccess, int worldX, int worldY, int worldZ) {
        // blocks can change opacity
        // blocks can change emitted light
        // blocks can change direction of propagation
        int encodeOffset = this.coordinateOffset;
        int currentLevel = this.getLightLevel(worldX, worldY, worldZ);
        BlockState state = this.getBlockState(worldX, worldY, worldZ);
        int emittedLevel = this.getEmittedLight(worldX, worldY, worldZ, state);
        this.setLightLevel(worldX, worldY, worldZ, emittedLevel);
        // this accounts for change in emitted light that would cause an increase
        if (emittedLevel != 0) {
            this.appendToIncreaseQueue(
                    worldX + ((long) worldZ << 6) + ((long) worldY << 12) + encodeOffset & (1L << 28) - 1
                    | (emittedLevel & 0x7FFFL) << 28
                    | (long) ALL_DIRECTIONS_BITSET << 43
                    | (state.isConditionallyFullOpaque() ? FLAG_HAS_SIDED_TRANSPARENT_BLOCKS : 0)
            );
        }
        // this also accounts for a change in emitted light that would cause a decrease
        // this also accounts for the change of direction of propagation (i.e. old block was full transparent, new block is full opaque or vice versa)
        // as it checks all neighbours (even if current level is 0)
        this.appendToDecreaseQueue(
                worldX + ((long) worldZ << 6) + ((long) worldY << 12) + encodeOffset & (1L << 28) - 1
                | (currentLevel & 0x7FFFL) << 28
                | (long) ALL_DIRECTIONS_BITSET << 43
                // always keep sided transparent false here, new block might be conditionally transparent which would
                // prevent us from decreasing sources in the directions where the new block is opaque
                // if it turns out we were wrong to de-propagate the source, the re-propagate logic WILL always
                // catch that and fix it.
        );
        // re-propagating neighbours (done by the decrease queue) will also account for opacity changes in this block
    }

    private int getEmittedLight(int x, int y, int z, BlockState state) {
        int dl = 0;
        if (this.level.isClientSide) {
            dl = ClientEvents.getInstance().getDynamicLights().getLight(BlockPos.asLong(x, y, z));
            if (dl == 0b1_1111_1_1111_1_1111) {
                return 0b1_1111_1_1111_1_1111;
            }
        }
        if (dl == 0) {
            return state.getLightEmission() & this.emittedLightMask;
        }
        return DynamicLights.combine(state.getLightEmission() & this.emittedLightMask, dl & this.emittedLightMask);
    }

    @Override
    protected boolean @Nullable [] getEmptinessMap(ChunkAccess chunk) {
        return chunk.getBlockEmptinessMap();
    }

    @Override
    protected SWMRShortArray[] getFilledEmptyDataStructure(int totalLightSections) {
        return getFilledEmptyLightShort(totalLightSections);
    }

    @Override
    protected SWMRShortArray[] getNibblesOnChunk(ChunkAccess chunk) {
        return chunk.getBlockShorts();
    }

    @Override
    protected void initNibble(int chunkX, int chunkY, int chunkZ, boolean extrude, boolean initRemovedNibbles) {
        if (chunkY < this.minLightSection || chunkY > this.maxLightSection || this.getChunkInCache(chunkX, chunkZ) == null) {
            return;
        }
        SWMRShortArray nibble = this.getNibbleFromCache(chunkX, chunkY, chunkZ);
        if (nibble == null) {
            if (!initRemovedNibbles) {
                throw new IllegalStateException();
            }
            this.setNibbleInCache(chunkX, chunkY, chunkZ, new SWMRShortArray());
        }
        else {
            nibble.setNonNull();
        }
    }

    @Override
    public void lightChunk(LightChunkGetter lightAccess, ChunkAccess chunk, boolean needsEdgeChecks) {
        LList sources = null;
        if (chunk instanceof ImposterProtoChunk || chunk instanceof LevelChunk) {
            // implementation on Chunk is pretty awful, so write our own here. The big optimisation is
            // skipping empty sections, and the far more optimised reading of types.
            int offX = chunk.getPos().x << 4;
            int offZ = chunk.getPos().z << 4;
            LevelChunkSection[] sections = chunk.getSections();
            for (int sectionY = this.minSection; sectionY <= this.maxSection; ++sectionY) {
                LevelChunkSection section = sections[sectionY - this.minSection];
                if (section == null || section.hasOnlyAir()) {
                    // no sources in empty sections
                    continue;
                }
                PalettedContainer<BlockState> states = section.states;
                int offY = sectionY << 4;
                for (int index = 0; index < 16 * 16 * 16; ++index) {
                    BlockState state = states.get(index);
                    if (state.getLightEmission() <= 0) {
                        continue;
                    }
                    if (sources == null) {
                        sources = new LArrayList();
                    }
                    sources.add(BlockPos.asLong(offX | index & 15, offY | index >>> 8, offZ | index >>> 4 & 15));
                }
            }

        }
        else {
            // world gen and lighting run in parallel, and if lighting keeps up it can be lighting chunks that are
            // being generated. In the nether, lava will add a lot of sources. This resulted in quite a few CME crashes.
            // So all we do spin loop until we can collect a list of sources, and even if it is out of date we will pick up
            // the missing sources from checkBlock.
            while (true) {
                try {
                    sources = chunk.getLights_();
                    break;
                }
                catch (final Exception ignored) {
                }
            }
        }
        // setup sources
        if (sources != null) {
            for (int i = 0, len = sources.size(); i < len; ++i) {
                long pos = sources.getLong(i);
                int x = BlockPos.getX(pos);
                int y = BlockPos.getY(pos);
                int z = BlockPos.getZ(pos);
                BlockState blockState = this.getBlockState(x, y, z);
                int emittedLight = blockState.getLightEmission() & this.emittedLightMask;
                if (emittedLight <= this.getLightLevel(x, y, z)) {
                    // some other source is brighter
                    continue;
                }
                this.appendToIncreaseQueue(
                        x + ((long) z << 6) + ((long) y << 12) + this.coordinateOffset & (1L << 28) - 1
                        | (emittedLight & 0x7FFFL) << 28
                        | (long) ALL_DIRECTIONS_BITSET << 43
                        | (blockState.isConditionallyFullOpaque() ? FLAG_HAS_SIDED_TRANSPARENT_BLOCKS : 0)
                );
                // propagation won't set this for us
                this.setLightLevel(x, y, z, emittedLight);
            }
        }
        if (needsEdgeChecks) {
            // not required to propagate here, but this will reduce the hit of the edge checks
            this.performLightIncrease(lightAccess);
            // verify neighbour edges
            this.checkChunkEdges(lightAccess, chunk, this.minLightSection, this.maxLightSection);
        }
        else {
            this.propagateNeighbourLevels(chunk, this.minLightSection, this.maxLightSection);
            this.performLightIncrease(lightAccess);
        }
    }

    @Override
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
        boolean server = level instanceof ServerLevel;
        if (server) {
            Evolution.info("block decrease queueLength = {}", printed);
        }
        while (queueReadIndex < queueLength) {
            if (server && queueLength > printed * 1.1) {
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
                    SWMRShortArray currentNibble = this.get(sectionIndex);
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
                        //Cached
                        int targetLevel = DynamicLights.decreaseLightComponents(propagatedLightLevel, Math.max(1, opacityCached));
                        int currentRed = currentLevel & 0b1_1111;
                        int targetRed = targetLevel & 0b1_1111;
                        boolean isRedGreater = DynamicLights.isLightGreater(currentRed, targetRed);
                        int currentGreen = currentLevel & 0b1_1111_0_0000;
                        int targetGreen = targetLevel & 0b1_1111_0_0000;
                        boolean isGreenGreater = DynamicLights.isLightGreater(currentGreen, targetGreen);
                        int currentBlue = currentLevel & 0b1_1111_0_0000_0_0000;
                        int targetBlue = targetLevel & 0b1_1111_0_0000_0_0000;
                        boolean isBlueGreater = DynamicLights.isLightGreater(currentBlue, targetBlue);
                        int repropagating = 0;
                        if (isRedGreater || isGreenGreater || isBlueGreater) {
                            boolean finished = true;
                            if (isRedGreater) {
                                repropagating = currentRed;
                            }
                            else {
                                finished = false;
                            }
                            if (isGreenGreater) {
                                repropagating |= currentGreen;
                            }
                            else {
                                finished = false;
                            }
                            if (isBlueGreater) {
                                repropagating |= currentBlue;
                            }
                            else {
                                finished = false;
                            }
                            //It looks like another source propagated here, so re-propagate it
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] = offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                                                   | (repropagating & 0x7FFFL) << 28
                                                                   | (long) ALL_DIRECTIONS_BITSET << 43
                                                                   | FLAG_RECHECK_LEVEL;
                            if (finished) {
                                continue;
                            }
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
                        currentNibble.set(localIndex, repropagating);
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
                    int targetLevel = DynamicLights.decreaseLightComponents(propagatedLightLevel, Math.max(1, opacity));
                    int currentRed = currentLevel & 0b1_1111;
                    int targetRed = targetLevel & 0b1_1111;
                    boolean isRedGreater = DynamicLights.isLightGreater(currentRed, targetRed);
                    int currentGreen = currentLevel & 0b1_1111_0_0000;
                    int targetGreen = targetLevel & 0b1_1111_0_0000;
                    boolean isGreenGreater = DynamicLights.isLightGreater(currentGreen, targetGreen);
                    int currentBlue = currentLevel & 0b1_1111_0_0000_0_0000;
                    int targetBlue = targetLevel & 0b1_1111_0_0000_0_0000;
                    boolean isBlueGreater = DynamicLights.isLightGreater(currentBlue, targetBlue);
                    long l = offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1;
                    int repropagating = 0;
                    if (isRedGreater || isGreenGreater || isBlueGreater) {
                        // it looks like another source propagated here, so re-propagate it
                        boolean finished = true;
                        if (isRedGreater) {
                            repropagating = currentRed;
                        }
                        else {
                            finished = false;
                        }
                        if (isGreenGreater) {
                            repropagating |= currentGreen;
                        }
                        else {
                            finished = false;
                        }
                        if (isBlueGreater) {
                            repropagating |= currentBlue;
                        }
                        else {
                            finished = false;
                        }
                        if (increaseQueueLength >= increaseQueue.length) {
                            increaseQueue = this.resizeIncreaseQueue();
                        }
                        increaseQueue[increaseQueueLength++] =
                                l
                                | (repropagating & 0x7FFFL) << 28
                                | (long) ALL_DIRECTIONS_BITSET << 43
                                | FLAG_RECHECK_LEVEL | flags;
                        if (finished) {
                            continue;
                        }
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
                    currentNibble.set(localIndex, repropagating);
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
                    SWMRShortArray currentNibble = this.get(sectionIndex);
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
                        //Cached
                        int targetLevel = DynamicLights.decreaseLightComponents(propagatedLightLevel, Math.max(1, opacityCached));
                        int currentRed = currentLevel & 0b1_1111;
                        int targetRed = targetLevel & 0b1_1111;
                        boolean isRedGreater = DynamicLights.isLightGreater(currentRed, targetRed);
                        int currentGreen = currentLevel & 0b1_1111_0_0000;
                        int targetGreen = targetLevel & 0b1_1111_0_0000;
                        boolean isGreenGreater = DynamicLights.isLightGreater(currentGreen, targetGreen);
                        int currentBlue = currentLevel & 0b1_1111_0_0000_0_0000;
                        int targetBlue = targetLevel & 0b1_1111_0_0000_0_0000;
                        boolean isBlueGreater = DynamicLights.isLightGreater(currentBlue, targetBlue);
                        int repropagating = 0;
                        if (isRedGreater || isGreenGreater || isBlueGreater) {
                            // it looks like another source propagated here, so re-propagate it
                            boolean finished = true;
                            if (isRedGreater) {
                                repropagating = currentRed;
                            }
                            else {
                                finished = false;
                            }
                            if (isGreenGreater) {
                                repropagating |= currentGreen;
                            }
                            else {
                                finished = false;
                            }
                            if (isBlueGreater) {
                                repropagating |= currentBlue;
                            }
                            else {
                                finished = false;
                            }
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                    | (repropagating & 0x7FFFL) << 28
                                    | (long) ALL_DIRECTIONS_BITSET << 43
                                    | FLAG_RECHECK_LEVEL;
                            if (finished) {
                                continue;
                            }
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
                        currentNibble.set(localIndex, repropagating);
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
                    int targetLevel = DynamicLights.decreaseLightComponents(propagatedLightLevel, Math.max(1, opacity));
                    int currentRed = currentLevel & 0b1_1111;
                    int targetRed = targetLevel & 0b1_1111;
                    boolean isRedGreater = DynamicLights.isLightGreater(currentRed, targetRed);
                    int currentGreen = currentLevel & 0b1_1111_0_0000;
                    int targetGreen = targetLevel & 0b1_1111_0_0000;
                    boolean isGreenGreater = DynamicLights.isLightGreater(currentGreen, targetGreen);
                    int currentBlue = currentLevel & 0b1_1111_0_0000_0_0000;
                    int targetBlue = targetLevel & 0b1_1111_0_0000_0_0000;
                    boolean isBlueGreater = DynamicLights.isLightGreater(currentBlue, targetBlue);
                    int repropagating = 0;
                    if (isRedGreater || isGreenGreater || isBlueGreater) {
                        // it looks like another source propagated here, so re-propagate it
                        boolean finished = true;
                        if (isRedGreater) {
                            repropagating = currentRed;
                        }
                        else {
                            finished = false;
                        }
                        if (isGreenGreater) {
                            repropagating |= currentGreen;
                        }
                        else {
                            finished = false;
                        }
                        if (isBlueGreater) {
                            repropagating |= currentBlue;
                        }
                        else {
                            finished = false;
                        }
                        if (increaseQueueLength >= increaseQueue.length) {
                            increaseQueue = this.resizeIncreaseQueue();
                        }
                        increaseQueue[increaseQueueLength++] =
                                offX + ((long) offZ << 6) + ((long) offY << 12) + encodeOffset & (1L << 28) - 1
                                | (repropagating & 0x7FFFL) << 28
                                | (long) ALL_DIRECTIONS_BITSET << 43
                                | FLAG_RECHECK_LEVEL | flags;
                        if (finished) {
                            continue;
                        }
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
                    currentNibble.set(localIndex, repropagating);
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

    @Override
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
        Result result = new Result(queue, queueLength);
        int printed = queueLength;
        boolean server = world instanceof ServerLevel;
        if (server) {
            Evolution.info("block increase queueLength = {}", printed);
        }
        while (queueReadIndex < queueLength) {
            if (server && queueLength > printed * 1.1) {
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
                result.queue = queue;
                result.queueLength = queueLength;
                this.shapeDoesntMatter(posX, posY, posZ, checkDirections, sectionOffset, propagatedLightLevel, encodeOffset, world, result);
                queue = result.queue;
                queueLength = result.queueLength;
            }
            else {
                // we actually need to worry about our state here
                result.queue = queue;
                result.queueLength = queueLength;
                this.shapeMatters(posX, posY, posZ, checkDirections, world, sectionOffset, propagatedLightLevel, encodeOffset, result);
                queue = result.queue;
                queueLength = result.queueLength;
            }
        }
    }

    @Override
    protected void propagateBlockChanges(LightChunkGetter lightAccess, ChunkAccess atChunk, LSet positions) {
        for (LongIterator it = positions.iterator(); it.hasNext(); ) {
            long pos = it.nextLong();
            this.checkBlock(lightAccess, BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos));
        }
        this.performLightDecrease(lightAccess);
    }

    @Override
    protected void setEmptinessMap(ChunkAccess chunk, boolean[] to) {
        chunk.setBlockEmptinessMap(to);
    }

    @Override
    protected void setNibbleNull(int chunkX, int chunkY, int chunkZ) {
        SWMRShortArray nibble = this.getNibbleFromCache(chunkX, chunkY, chunkZ);
        if (nibble != null) {
            // de-initialisation is not as straightforward as with sky data, since deinit of block light is typically
            // because a block was removed - which can decrease light. with sky data, block breaking can only result
            // in increases, and thus the existing sky block check will actually correctly propagate light through
            // a null section. so in order to propagate decreases correctly, we can do a couple of things: not remove
            // the data section, or do edge checks on ALL axis (x, y, z). however I do not want edge checks running
            // for clients at all, as they are expensive. so we don't remove the section, but to maintain the appearance
            // of vanilla data management we "hide" them.
            nibble.setHidden();
        }
    }

    @Override
    protected void setNibbles(ChunkAccess chunk, SWMRShortArray[] to) {
        chunk.setBlockShorts(to);
    }

    private void shapeDoesntMatter(int posX, int posY, int posZ, AxisDirection[] checkDirections, int sectionOffset, int propagatedLightLevel, int encodeOffset, BlockGetter world, Result result) {
        long[] queue = result.queue;
        int queueLength = result.queueLength;
        for (AxisDirection propagate : checkDirections) {
            int offX = posX + propagate.x;
            int offY = posY + propagate.y;
            int offZ = posZ + propagate.z;
            int sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + 5 * 5 * (offY >> 4) + sectionOffset;
            int localIndex = offX & 15 | (offZ & 15) << 4 | (offY & 15) << 8;
            SWMRShortArray currentNibble = this.get(sectionIndex);
            if (currentNibble == null) {
                //Unloaded
                continue;
            }
            int currentLevel = currentNibble.getUpdating(localIndex);
            if (DynamicLights.isLightAtLeastAsGreatAs(currentLevel, DynamicLights.decreaseLightComponents(propagatedLightLevel, 1))) {
                //Already at the level we want or more
                continue;
            }
            BlockState stateAt = this.getBlockState(sectionIndex, localIndex);
            int opacityCached = stateAt.getOpacityIfCached();
            if (opacityCached != -1) {
                //Cached
                int targetLevel = DynamicLights.decreaseLightComponents(propagatedLightLevel, Math.max(1, opacityCached));
                if (DynamicLights.isLightGreater(targetLevel, currentLevel)) {
                    targetLevel = DynamicLights.combine(targetLevel, currentLevel);
                    currentNibble.set(localIndex, targetLevel);
                    this.postLightUpdate(offX, offY, offZ);
                    if (DynamicLights.canSpread(targetLevel)) {
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
            int targetLevel = DynamicLights.decreaseLightComponents(propagatedLightLevel, Math.max(1, opacity));
            if (!DynamicLights.isLightGreater(targetLevel, currentLevel)) {
                continue;
            }
            targetLevel = DynamicLights.combine(targetLevel, currentLevel);
            currentNibble.set(localIndex, targetLevel);
            this.postLightUpdate(offX, offY, offZ);
            if (DynamicLights.canSpread(targetLevel)) {
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
        result.queue = queue;
        result.queueLength = queueLength;
    }

    private void shapeMatters(int posX, int posY, int posZ, AxisDirection[] checkDirections, BlockGetter world, int sectionOffset, int propagatedLightLevel, int encodeOffset, Result result) {
        long[] queue = result.queue;
        int queueLength = result.queueLength;
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
            SWMRShortArray currentNibble = this.get(sectionIndex);
            if (currentNibble == null) {
                //Unloaded
                continue;
            }
            int currentLevel = currentNibble.getUpdating(localIndex);
            if (DynamicLights.isLightAtLeastAsGreatAs(currentLevel, DynamicLights.decreaseLightComponents(propagatedLightLevel, 1))) {
                //Already at the level we want or more
                continue;
            }
            BlockState stateAt = this.getBlockState(sectionIndex, localIndex);
            int opacityCached = stateAt.getOpacityIfCached();
            if (opacityCached != -1) {
                //Cached
                int targetLevel = DynamicLights.decreaseLightComponents(propagatedLightLevel, Math.max(1, opacityCached));
                if (DynamicLights.isLightGreater(targetLevel, currentLevel)) {
                    targetLevel = DynamicLights.combine(targetLevel, currentLevel);
                    currentNibble.set(localIndex, targetLevel);
                    this.postLightUpdate(offX, offY, offZ);
                    if (DynamicLights.canSpread(targetLevel)) {
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
            int targetLevel = DynamicLights.decreaseLightComponents(propagatedLightLevel, Math.max(1, opacity));
            if (!DynamicLights.isLightGreater(targetLevel, currentLevel)) {
                continue;
            }
            targetLevel = DynamicLights.combine(targetLevel, currentLevel);
            currentNibble.set(localIndex, targetLevel);
            this.postLightUpdate(offX, offY, offZ);
            if (DynamicLights.canSpread(targetLevel)) {
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
        result.queue = queue;
        result.queueLength = queueLength;
    }

    private static class Result {
        public long[] queue;
        public int queueLength;

        public Result(long[] queue, int queueLength) {
            this.queue = queue;
            this.queueLength = queueLength;
        }
    }
}
