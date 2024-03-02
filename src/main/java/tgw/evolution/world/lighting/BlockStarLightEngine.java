package tgw.evolution.world.lighting;

import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.renderer.ambient.DynamicLights;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.collection.lists.LArrayList;
import tgw.evolution.util.collection.lists.LList;
import tgw.evolution.util.collection.sets.LSet;

import java.util.Arrays;

public final class BlockStarLightEngine extends StarLightEngine<SWMRShortArray> {

    private final long[][] decrQueue = new long[3][];
    private final int[] decrQueueLen = new int[3];
    private final long[][] incrQueue = new long[3][];
    private final int[] incrQueueLen = new int[3];

    public BlockStarLightEngine(Level level) {
        super(false, level);
        for (int i = 0; i < 3; ++i) {
            //noinspection ObjectAllocationInLoop
            this.decrQueue[i] = new long[16];
            //noinspection ObjectAllocationInLoop
            this.incrQueue[i] = new long[16];
        }
    }

    private void appendToDecrQueue(@RGBFlag int colour, int x, int y, int z, int encodeOffset, int light, int directions, long flags) {
        long encoded = encodeToQueue(x, y, z, encodeOffset, light, directions, flags);
        for (@RGB int i = RGB.RED; i < 3; ++i) {
            if ((colour & 1 << i) != 0) {
                int index = this.decrQueueLen[i]++;
                long[] queue = this.decrQueue[i];
                if (index >= queue.length) {
                    queue = this.resizeDecreaseQueue(i);
                }
                queue[index] = encoded;
            }
        }
    }

    private void appendToDecreaseQueue(int x, int y, int z, int encodeOffset, int light, int directions, long flags) {
        this.appendToDecrQueue(RGBFlag.RED | RGBFlag.GREEN | RGBFlag.BLUE, x, y, z, encodeOffset, light, directions, flags);
    }

    private void appendToIncrQueue(@RGBFlag int colour, int x, int y, int z, int encodeOffset, int light, int directions, long flags) {
        long encoded = encodeToQueue(x, y, z, encodeOffset, light, directions, flags);
        for (@RGB int i = RGB.RED; i < 3; ++i) {
            if ((colour & 1 << i) != 0) {
                int index = this.incrQueueLen[i]++;
                long[] queue = this.incrQueue[i];
                if (index >= queue.length) {
                    queue = this.resizeIncreaseQueue(i);
                }
                queue[index] = encoded;
            }
        }
    }

    @Override
    protected void appendToIncreaseQueue(int x, int y, int z, int encodeOffset, int light, int directions, long flags) {
        this.appendToIncrQueue(RGBFlag.RED | RGBFlag.GREEN | RGBFlag.BLUE, x, y, z, encodeOffset, light, directions, flags);
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
    protected void checkBlock(LightChunkGetter lightAccess, int x, int y, int z) {
        // blocks can change opacity
        // blocks can change emitted light
        // blocks can change direction of propagation
        int encodeOffset = this.coordinateOffset;
        int currentLevel = this.getLightLevel(x, y, z);
        BlockState state = this.getBlockState(x, y, z);
        int emittedLevel = this.getEmittedLight(x, y, z, state);
        this.setLightLevel(x, y, z, emittedLevel);
        // this accounts for change in emitted light that would cause an increase
        if (emittedLevel != 0) {
            this.appendToIncreaseQueue(x, y, z, encodeOffset, emittedLevel, ALL_DIRECTIONS_BITSET, state.isConditionallyFullOpaque() ? FLAG_HAS_SIDED_TRANSPARENT_BLOCKS : 0);
        }
        // this also accounts for a change in emitted light that would cause a decrease
        // this also accounts for the change of direction of propagation (i.e. old block was full transparent, new block is full opaque or vice versa)
        // as it checks all neighbours (even if current level is 0).
        // Always keep sided transparent false here, new block might be conditionally transparent which would
        // prevent us from decreasing sources in the directions where the new block is opaque
        // if it turns out we were wrong to de-propagate the source, the re-propagate logic WILL always
        // catch that and fix it.
        this.appendToDecreaseQueue(x, y, z, encodeOffset, currentLevel, ALL_DIRECTIONS_BITSET, 0);
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
                this.appendToIncreaseQueue(x, y, z, this.coordinateOffset, emittedLight, ALL_DIRECTIONS_BITSET, blockState.isConditionallyFullOpaque() ? FLAG_HAS_SIDED_TRANSPARENT_BLOCKS : 0);
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

    private void performLightDecrease(@RGB int colour, BlockGetter level) {
        int qReadIndex = 0;
        int decodeOffsetX = -this.encodeOffsetX;
        int decodeOffsetY = -this.encodeOffsetY;
        int decodeOffsetZ = -this.encodeOffsetZ;
        int encodeOffset = this.coordinateOffset;
        int sectionOffset = this.chunkSectionIndexOffset;
        int emittedMask = this.emittedLightMask;
        long[] q = this.decrQueue[colour];
        int qLen = this.decrQueueLen[colour];
        this.decrQueueLen[colour] = 0;
        long[] incrQ = this.incrQueue[colour];
        int incrQLen = this.incrQueueLen[colour];
        while (qReadIndex < qLen) {
            long queueValue = q[qReadIndex++];
            int posX = decodeX(queueValue, decodeOffsetX);
            int posY = decodeY(queueValue, decodeOffsetY);
            int posZ = decodeZ(queueValue, decodeOffsetZ);
            int propagatedLight = decodeLight(queueValue);
            AxisDirection[] checkDirections = decodeDirections(queueValue);
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
                    int currentLight = currentNibble.getUpdating(localIndex);
                    int currentColour = DynamicLights.getComponent(currentLight, colour);
                    if (currentColour == 0) {
                        //Already at lowest, nothing to do
                        continue;
                    }
                    BlockState stateAt = this.getBlockState(sectionIndex, localIndex);
                    int opacityCached = stateAt.getOpacityIfCached();
                    if (opacityCached != -1) {
                        //Cached
                        int targetLight = DynamicLights.decreaseLight(propagatedLight, Math.max(1, opacityCached));
                        int targetColour = DynamicLights.getComponent(targetLight, colour);
                        if (DynamicLights.isComponentGreaterInRange(currentColour, targetColour)) {
                            //It looks like another source propagated here, so re-propagate it
                            if (incrQLen >= incrQ.length) {
                                incrQ = this.resizeIncreaseQueue(colour);
                            }
                            incrQ[incrQLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, currentLight, ALL_DIRECTIONS_BITSET, FLAG_RECHECK_LEVEL);
                            continue;
                        }
                        int emittedLight = stateAt.getLightEmission() & emittedMask;
                        int emittedColour = DynamicLights.getComponent(emittedLight, colour);
                        if (emittedColour != 0) {
                            // re-propagate source
                            // note: do not set recheck level, or else the propagation will fail
                            if (incrQLen >= incrQ.length) {
                                incrQ = this.resizeIncreaseQueue(colour);
                            }
                            incrQ[incrQLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, emittedLight, ALL_DIRECTIONS_BITSET, stateAt.isConditionallyFullOpaque() ? FLAG_WRITE_LEVEL | FLAG_HAS_SIDED_TRANSPARENT_BLOCKS : FLAG_WRITE_LEVEL);
                        }
                        currentNibble.set(localIndex, DynamicLights.removeComponent(currentLight, colour));
                        this.postLightUpdate(offX, offY, offZ);
                        if (targetColour > 0) {
                            if (qLen >= q.length) {
                                q = this.resizeDecreaseQueue(colour);
                            }
                            q[qLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLight, propagate.everythingButTheOppositeDirection, 0);
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
                    int targetLight = DynamicLights.decreaseLight(propagatedLight, Math.max(1, opacity));
                    int targetColour = DynamicLights.getComponent(targetLight, colour);
                    if (DynamicLights.isComponentGreaterInRange(currentColour, targetColour)) {
                        // it looks like another source propagated here, so re-propagate it
                        if (incrQLen >= incrQ.length) {
                            incrQ = this.resizeIncreaseQueue(colour);
                        }
                        incrQ[incrQLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, currentLight, ALL_DIRECTIONS_BITSET, FLAG_RECHECK_LEVEL | flags);
                        continue;
                    }
                    int emittedLight = stateAt.getLightEmission() & emittedMask;
                    int emittedColour = DynamicLights.getComponent(emittedLight, colour);
                    if (emittedColour != 0) {
                        // re-propagate source
                        // note: do not set recheck level, or else the propagation will fail
                        if (incrQLen >= incrQ.length) {
                            incrQ = this.resizeIncreaseQueue(colour);
                        }
                        incrQ[incrQLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, emittedLight, ALL_DIRECTIONS_BITSET, FLAG_WRITE_LEVEL | flags);
                    }
                    currentNibble.set(localIndex, DynamicLights.removeComponent(currentLight, colour));
                    this.postLightUpdate(offX, offY, offZ);
                    if (targetColour > 0) {
                        if (qLen >= q.length) {
                            q = this.resizeDecreaseQueue(colour);
                        }
                        q[qLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLight, propagate.everythingButTheOppositeDirection, flags);
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
                    int currentLight = currentNibble.getUpdating(localIndex);
                    int currentColour = DynamicLights.getComponent(currentLight, colour);
                    if (currentColour == 0) {
                        // already at lowest, nothing to do
                        continue;
                    }
                    BlockState stateAt = this.getBlockState(sectionIndex, localIndex);
                    int opacityCached = stateAt.getOpacityIfCached();
                    if (opacityCached != -1) {
                        //Cached
                        int targetLight = DynamicLights.decreaseLight(propagatedLight, Math.max(1, opacityCached));
                        int targetColour = DynamicLights.getComponent(targetLight, colour);
                        if (DynamicLights.isComponentGreaterInRange(currentColour, targetColour)) {
                            // it looks like another source propagated here, so re-propagate it
                            if (incrQLen >= incrQ.length) {
                                incrQ = this.resizeIncreaseQueue(colour);
                            }
                            incrQ[incrQLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, currentLight, ALL_DIRECTIONS_BITSET, FLAG_RECHECK_LEVEL);
                            continue;
                        }
                        int emittedLight = stateAt.getLightEmission() & emittedMask;
                        int emittedColour = DynamicLights.getComponent(emittedLight, colour);
                        if (emittedColour != 0) {
                            // re-propagate source
                            // note: do not set recheck level, or else the propagation will fail
                            if (incrQLen >= incrQ.length) {
                                incrQ = this.resizeIncreaseQueue(colour);
                            }
                            incrQ[incrQLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, emittedLight, ALL_DIRECTIONS_BITSET, stateAt.isConditionallyFullOpaque() ? FLAG_WRITE_LEVEL | FLAG_HAS_SIDED_TRANSPARENT_BLOCKS : FLAG_WRITE_LEVEL);
                        }
                        currentNibble.set(localIndex, DynamicLights.removeComponent(currentLight, colour));
                        this.postLightUpdate(offX, offY, offZ);
                        if (targetColour > 0) {
                            if (qLen >= q.length) {
                                q = this.resizeDecreaseQueue(colour);
                            }
                            q[qLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLight, propagate.everythingButTheOppositeDirection, 0);
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
                    int targetLight = DynamicLights.decreaseLight(propagatedLight, Math.max(1, opacity));
                    int targetColour = DynamicLights.getComponent(targetLight, colour);
                    if (DynamicLights.isComponentGreaterInRange(currentColour, targetColour)) {
                        // it looks like another source propagated here, so re-propagate it
                        if (incrQLen >= incrQ.length) {
                            incrQ = this.resizeIncreaseQueue(colour);
                        }
                        incrQ[incrQLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, currentLight, ALL_DIRECTIONS_BITSET, FLAG_RECHECK_LEVEL | flags);
                        continue;
                    }
                    int emittedLight = stateAt.getLightEmission() & emittedMask;
                    int emittedColour = DynamicLights.getComponent(emittedLight, colour);
                    if (emittedColour != 0) {
                        // re-propagate source
                        // note: do not set recheck level, or else the propagation will fail
                        if (incrQLen >= incrQ.length) {
                            incrQ = this.resizeIncreaseQueue(colour);
                        }
                        incrQ[incrQLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, emittedLight, ALL_DIRECTIONS_BITSET, flags | FLAG_WRITE_LEVEL);
                    }
                    currentNibble.set(localIndex, DynamicLights.removeComponent(currentLight, colour));
                    this.postLightUpdate(offX, offY, offZ);
                    if (targetColour > 0) { // we actually need to propagate 0 just in case we find a neighbour...
                        if (qLen >= q.length) {
                            q = this.resizeDecreaseQueue(colour);
                        }
                        q[qLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLight, propagate.everythingButTheOppositeDirection, flags);
                    }
                }
            }
        }
        // propagate sources we clobbered
        this.incrQueueLen[colour] = incrQLen;
    }

    @Override
    protected void performLightDecrease(LightChunkGetter lightAccess) {
        BlockGetter level = lightAccess.getLevel();
        this.performLightDecrease(RGB.RED, level);
        this.performLightDecrease(RGB.GREEN, level);
        this.performLightDecrease(RGB.BLUE, level);
        this.performLightIncrease(lightAccess);
    }

    private void performLightIncrease(@RGB int colour, BlockGetter level) {
        int qReadIndex = 0;
        int decodeOffsetX = -this.encodeOffsetX;
        int decodeOffsetY = -this.encodeOffsetY;
        int decodeOffsetZ = -this.encodeOffsetZ;
        int encodeOffset = this.coordinateOffset;
        int sectionOffset = this.chunkSectionIndexOffset;
        long[] q = this.incrQueue[colour];
        int qLen = this.incrQueueLen[colour];
        this.incrQueueLen[colour] = 0;
        while (qReadIndex < qLen) {
            long queueValue = q[qReadIndex++];
            int posX = decodeX(queueValue, decodeOffsetX);
            int posY = decodeY(queueValue, decodeOffsetY);
            int posZ = decodeZ(queueValue, decodeOffsetZ);
            int propagatedLight = decodeLight(queueValue);
            int propagatedColour = DynamicLights.getComponent(propagatedLight, colour);
            AxisDirection[] checkDirections = decodeDirections(queueValue);
            if ((queueValue & FLAG_RECHECK_LEVEL) != 0L) {
                if (DynamicLights.getComponent(this.getLightLevel(posX, posY, posZ), colour) != propagatedColour) {
                    // not at the level we expect, so something changed.
                    continue;
                }
            }
            else if ((queueValue & FLAG_WRITE_LEVEL) != 0L) {
                // these are used to restore block sources after a propagation decrease
                this.setLightLevel(posX, posY, posZ, propagatedLight);
            }
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
                        //Unloaded
                        continue;
                    }
                    int currentLight = currentNibble.getUpdating(localIndex);
                    int currentColour = DynamicLights.getComponent(currentLight, colour);
                    int comp = DynamicLights.decreaseComponent(propagatedColour, 1);
                    if (currentColour == comp || DynamicLights.isComponentTotallyGreater(currentColour, comp)) {
                        //Already at the level we want or more
                        continue;
                    }
                    BlockState stateAt = this.getBlockState(sectionIndex, localIndex);
                    int opacityCached = stateAt.getOpacityIfCached();
                    if (opacityCached != -1) {
                        //Cached
                        int targetLight = DynamicLights.decreaseLight(propagatedLight, Math.max(1, opacityCached));
                        int targetColour = DynamicLights.getComponent(targetLight, colour);
                        if (DynamicLights.isComponentGreaterInRange(targetColour, currentColour)) {
                            targetLight = DynamicLights.recombine(currentLight, targetColour, colour);
                            currentNibble.set(localIndex, targetLight);
                            this.postLightUpdate(offX, offY, offZ);
                            if (DynamicLights.canSpread(targetColour)) {
                                if (qLen >= q.length) {
                                    q = this.resizeIncreaseQueue(colour);
                                }
                                q[qLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLight, propagate.everythingButTheOppositeDirection, 0);
                                continue;
                            }
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
                    int targetLight = DynamicLights.decreaseLight(propagatedLight, Math.max(1, opacity));
                    int targetColour = DynamicLights.getComponent(targetLight, colour);
                    if (targetColour == currentColour || DynamicLights.isComponentGreaterInRange(currentColour, targetColour)) {
                        continue;
                    }
                    targetLight = DynamicLights.recombine(currentLight, targetColour, colour);
                    currentNibble.set(localIndex, targetLight);
                    this.postLightUpdate(offX, offY, offZ);
                    if (DynamicLights.canSpread(targetColour)) {
                        if (qLen >= q.length) {
                            q = this.resizeIncreaseQueue(colour);
                        }
                        q[qLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLight, propagate.everythingButTheOppositeDirection, flags);
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
                    VoxelShape fromShape = fromBlock.isConditionallyFullOpaque() ? fromBlock.getFaceOcclusionShape_(level, posX, posY, posZ, propagate.nms) : Shapes.empty();
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
                    int currentLight = currentNibble.getUpdating(localIndex);
                    int currentColour = DynamicLights.getComponent(currentLight, colour);
                    int comp = DynamicLights.decreaseComponent(propagatedColour, 1);
                    if (currentColour == comp || DynamicLights.isComponentGreaterInRange(currentColour, comp)) {
                        //Already at the level we want or more
                        continue;
                    }
                    BlockState stateAt = this.getBlockState(sectionIndex, localIndex);
                    int opacityCached = stateAt.getOpacityIfCached();
                    if (opacityCached != -1) {
                        //Cached
                        int targetLight = DynamicLights.decreaseLight(propagatedLight, Math.max(1, opacityCached));
                        int targetColour = DynamicLights.getComponent(targetLight, colour);
                        if (DynamicLights.isComponentGreaterInRange(targetColour, currentColour)) {
                            targetLight = DynamicLights.recombine(currentLight, targetColour, colour);
                            currentNibble.set(localIndex, targetLight);
                            this.postLightUpdate(offX, offY, offZ);
                            if (DynamicLights.canSpread(targetColour)) {
                                if (qLen >= q.length) {
                                    q = this.resizeIncreaseQueue(colour);
                                }
                                q[qLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLight, propagate.everythingButTheOppositeDirection, 0);
                                continue;
                            }
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
                    int targetLight = DynamicLights.decreaseLight(propagatedLight, Math.max(1, opacity));
                    int targetColour = DynamicLights.getComponent(targetLight, colour);
                    if (targetColour == currentColour || DynamicLights.isComponentGreaterInRange(currentColour, targetColour)) {
                        continue;
                    }
                    targetLight = DynamicLights.recombine(currentLight, targetColour, colour);
                    currentNibble.set(localIndex, targetLight);
                    this.postLightUpdate(offX, offY, offZ);
                    if (DynamicLights.canSpread(targetColour)) {
                        if (qLen >= q.length) {
                            q = this.resizeIncreaseQueue(colour);
                        }
                        q[qLen++] = encodeToQueue(offX, offY, offZ, encodeOffset, targetLight, propagate.everythingButTheOppositeDirection, flags);
                    }
                }
            }
        }
    }

    @Override
    protected void performLightIncrease(LightChunkGetter lightAccess) {
        BlockGetter level = lightAccess.getLevel();
        this.performLightIncrease(RGB.RED, level);
        this.performLightIncrease(RGB.GREEN, level);
        this.performLightIncrease(RGB.BLUE, level);
    }

    @Override
    protected void propagateBlockChanges(LightChunkGetter lightAccess, ChunkAccess atChunk, LSet positions) {
        for (LongIterator it = positions.iterator(); it.hasNext(); ) {
            long pos = it.nextLong();
            this.checkBlock(lightAccess, BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos));
        }
        this.performLightDecrease(lightAccess);
    }

    private long[] resizeDecreaseQueue(@RGB int colour) {
        long[] queue = this.decrQueue[colour];
        return this.decrQueue[colour] = Arrays.copyOf(queue, queue.length * 2);
    }

    private long[] resizeIncreaseQueue(@RGB int colour) {
        long[] queue = this.incrQueue[colour];
        return this.incrQueue[colour] = Arrays.copyOf(queue, queue.length * 2);
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
}
