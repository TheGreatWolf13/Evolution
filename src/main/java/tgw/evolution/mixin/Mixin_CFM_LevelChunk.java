package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.*;
import tgw.evolution.blocks.tileentities.TEUtils;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.capabilities.chunk.CapabilityChunkStorage;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchLevelChunk;
import tgw.evolution.patches.obj.IBlockEntityTagOutput;
import tgw.evolution.util.ChunkHolder;
import tgw.evolution.util.collection.ArrayHelper;
import tgw.evolution.util.collection.lists.IArrayList;
import tgw.evolution.util.collection.lists.IList;
import tgw.evolution.util.collection.lists.LArrayList;
import tgw.evolution.util.collection.lists.LList;
import tgw.evolution.util.collection.maps.L2OHashMap;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.collection.maps.R2OMap;
import tgw.evolution.util.collection.sets.IHashSet;
import tgw.evolution.util.collection.sets.ISet;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionList;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.PropagationDirection;
import tgw.evolution.world.lighting.StarLightEngine;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(LevelChunk.class)
public abstract class Mixin_CFM_LevelChunk extends ChunkAccess implements PatchLevelChunk {

    @Shadow @Final static Logger LOGGER;
    @Unique private static final ThreadLocal<IList> TO_UPDATE = ThreadLocal.withInitial(IArrayList::new);
    @Unique private static final ThreadLocal<ISet> AUX_SET = ThreadLocal.withInitial(IHashSet::new);
    @Unique private static final ThreadLocal<ChunkHolder> HOLDER = ThreadLocal.withInitial(ChunkHolder::new);
    @Shadow @Final private static TickingBlockEntity NULL_TICKER;
    @Mutable @Shadow @Final @RestoreFinal public Level level;
    @Mutable @Shadow @Final @RestoreFinal private LevelChunkTicks<Block> blockTicks;
    @Unique private final CapabilityChunkStorage chunkStorage;
    @Shadow private boolean clientLightReady;
    @Mutable @Shadow @Final @RestoreFinal private LevelChunkTicks<Fluid> fluidTicks;
    @Mutable @Shadow @Final @RestoreFinal private Int2ObjectMap<GameEventDispatcher> gameEventDispatcherSections;
    @Shadow private @Nullable LevelChunk.PostLoadProcessor postLoad;
    @Shadow @Final @DeleteField private Map<BlockPos, LevelChunk.RebindableTickingBlockEntityWrapper> tickersInLevel;
    @Unique private final L2OMap<LevelChunk.RebindableTickingBlockEntityWrapper> tickersInLevel_;

    @ModifyConstructor
    public Mixin_CFM_LevelChunk(Level level, ChunkPos chunkPos, UpgradeData upgradeData, LevelChunkTicks<Block> blockTicks, LevelChunkTicks<Fluid> fluidTicks, long inhabitedTime, @Nullable LevelChunkSection[] sections, @Nullable LevelChunk.PostLoadProcessor processor, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, level, level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), inhabitedTime, sections, blendingData);
        this.tickersInLevel_ = new L2OHashMap<>();
        this.clientLightReady = false;
        this.level = level;
        this.chunkStorage = level.isClientSide ? CapabilityChunkStorage.CLIENT : new CapabilityChunkStorage();
        this.gameEventDispatcherSections = new Int2ObjectOpenHashMap<>();
        for (Heightmap.Types types : ArrayHelper.HEIGHTMAP) {
            if (ChunkStatus.FULL.heightmapsAfter().contains(types)) {
                //noinspection ObjectAllocationInLoop
                this.heightmaps_().put(types, new Heightmap(this, types));
            }
        }
        this.postLoad = processor;
        this.blockTicks = blockTicks;
        this.fluidTicks = fluidTicks;
        this.setBlockShorts(StarLightEngine.getFilledEmptyLightShort(level));
        this.setSkyNibbles(StarLightEngine.getFilledEmptyLightNibble(level));
    }

    @ModifyConstructor
    public Mixin_CFM_LevelChunk(ServerLevel level, ProtoChunk protoChunk, @Nullable LevelChunk.PostLoadProcessor processor) {
        this(level, protoChunk.getPos(), protoChunk.getUpgradeData(), protoChunk.unpackBlockTicks(), protoChunk.unpackFluidTicks(), protoChunk.getInhabitedTime(), protoChunk.getSections(), processor, protoChunk.getBlendingData());
        L2OMap<BlockEntity> tes = protoChunk.blockEntities_();
        for (long it = tes.beginIteration(); tes.hasNextIteration(it); it = tes.nextEntry(it)) {
            this.setBlockEntity(tes.getIterationValue(it));
        }
        this.pendingBlockEntities_().putAll(protoChunk.pendingBlockEntities_());
        for (int i = 0; i < protoChunk.getPostProcessing().length; ++i) {
            this.postProcessing[i] = protoChunk.getPostProcessing()[i];
        }
        this.setAllStarts(protoChunk.getAllStarts());
        this.setAllReferences(protoChunk.getAllReferences());
        for (Map.Entry<Heightmap.Types, Heightmap> entry : protoChunk.heightmaps_().entrySet()) {
            if (ChunkStatus.FULL.heightmapsAfter().contains(entry.getKey())) {
                this.setHeightmap(entry.getKey(), entry.getValue().getRawData());
            }
        }
        this.setLightCorrect(protoChunk.isLightCorrect());
        this.unsaved = true;
        this.setBlockShorts(protoChunk.getBlockShorts());
        this.setSkyNibbles(protoChunk.getSkyNibbles());
        this.setSkyEmptinessMap(protoChunk.getSkyEmptinessMap());
        this.setBlockEmptinessMap(protoChunk.getBlockEmptinessMap());
//        this.primeStructural(false);
        this.primeAtm(false);
    }

    @Shadow
    public abstract void addAndRegisterBlockEntity(BlockEntity pBlockEntity);

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Overwrite
    public void clearAllBlockEntities() {
        L2OMap<BlockEntity> tes = this.blockEntities_();
        for (long it = tes.beginIteration(); tes.hasNextIteration(it); it = tes.nextEntry(it)) {
            tes.getIterationValue(it).setRemoved();
        }
        tes.clear();
        L2OMap<LevelChunk.RebindableTickingBlockEntityWrapper> tickers = this.tickersInLevel_;
        for (long it = tickers.beginIteration(); tickers.hasNextIteration(it); it = tickers.nextEntry(it)) {
            tickers.getIterationValue(it).rebind(NULL_TICKER);
        }
        tickers.clear();
    }

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Overwrite
    public Map<BlockPos, BlockEntity> getBlockEntities() {
        Evolution.deprecatedMethod();
        return Map.of();
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @SuppressWarnings("removal")
    @Overwrite
    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockEntity_(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    public @Nullable BlockEntity getBlockEntity(BlockPos pos, LevelChunk.EntityCreationType creationType) {
        Evolution.deprecatedMethod();
        return this.getBlockEntity_(pos.getX(), pos.getY(), pos.getZ(), creationType);
    }

    /**
     * @author TheGreatWolf
     * @reason Call non-BlockPos version
     */
    @SuppressWarnings("removal")
    @Overwrite
    @Override
    public @Nullable CompoundTag getBlockEntityNbtForSaving(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockEntityNbtForSaving_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public @Nullable CompoundTag getBlockEntityNbtForSaving_(int x, int y, int z) {
        BlockEntity tile = this.getBlockEntity_(x, y, z);
        CompoundTag tag;
        if (tile != null && !tile.isRemoved()) {
            tag = tile.saveWithFullMetadata();
            tag.putBoolean("keepPacked", false);
            return tag;
        }
        tag = this.pendingBlockEntities_().get(BlockPos.asLong(x, y, z));
        if (tag != null) {
            tag = tag.copy();
            tag.putBoolean("keepPacked", true);
        }
        return tag;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z, LevelChunk.EntityCreationType creationType) {
        long pos = BlockPos.asLong(x, y, z);
        BlockEntity te = this.blockEntities_().get(pos);
        if (te == null) {
            CompoundTag tag = this.pendingBlockEntities_().remove(pos);
            if (tag != null) {
                BlockEntity newTe = this.promotePendingBlockEntity_(x, y, z, tag);
                if (newTe != null) {
                    return newTe;
                }
            }
            if (creationType == LevelChunk.EntityCreationType.IMMEDIATE) {
                te = this.createBlockEntity_(x, y, z);
                if (te != null) {
                    this.addAndRegisterBlockEntity(te);
                }
            }
        }
        if (te != null && te.isRemoved()) {
            this.blockEntities_().remove(pos);
            return null;
        }
        return te;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z) {
        return this.getBlockEntity_(x, y, z, LevelChunk.EntityCreationType.CHECK);
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @SuppressWarnings("removal")
    @Overwrite
    @Override
    public BlockState getBlockState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BlockState getBlockState_(int x, int y, int z) {
        if (this.level.isDebug()) {
            if (y == 60) {
                return Blocks.BARRIER.defaultBlockState();
            }
            if (y == 70) {
                return DebugLevelSource.getBlockStateFor(x, z);
            }
            return Blocks.AIR.defaultBlockState();
        }
        try {
            int index = this.getSectionIndex(y);
            if (index >= 0 && index < this.sections.length) {
                LevelChunkSection section = this.sections[index];
                if (!section.hasOnlyAir()) {
                    return section.getBlockState(x & 15, y & 15, z & 15);
                }
            }
            return Blocks.AIR.defaultBlockState();
        }
        catch (Throwable t) {
            CrashReport crash = CrashReport.forThrowable(t, "Getting block state");
            CrashReportCategory category = crash.addCategory("Block being got");
            category.setDetail("Location", () -> CrashReportCategory.formatLocation(this, x, y, z));
            throw new ReportedException(crash);
        }
    }

    @Override
    public CapabilityChunkStorage getChunkStorage() {
        return this.chunkStorage;
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version.
     */
    @SuppressWarnings("removal")
    @Overwrite
    @Override
    public FluidState getFluidState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getFluidState_(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @author TheGreatWolf
     * @reason Delegate to our implementation
     */
    @Overwrite
    public FluidState getFluidState(int x, int y, int z) {
        return this.getFluidState_(x, y, z);
    }

    @Override
    public FluidState getFluidState_(int x, int y, int z) {
        try {
            int index = this.getSectionIndex(y);
            if (index >= 0 && index < this.sections.length) {
                LevelChunkSection section = this.sections[index];
                if (!section.hasOnlyAir()) {
                    return section.getFluidState(x & 15, y & 15, z & 15);
                }
            }
            return Fluids.EMPTY.defaultFluidState();
        }
        catch (Throwable t) {
            CrashReport crash = CrashReport.forThrowable(t, "Getting fluid state");
            CrashReportCategory category = crash.addCategory("Block being got");
            category.setDetail("Location", () -> CrashReportCategory.formatLocation(this, x, y, z));
            throw new ReportedException(crash);
        }
    }

    @Shadow
    public abstract net.minecraft.server.level.ChunkHolder.FullChunkStatus getFullStatus();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @SuppressWarnings("removal")
    @Override
    @Overwrite
    public Stream<BlockPos> getLights() {
        Evolution.deprecatedMethod();
        return this.getLights_().longStream().mapToObj(BlockPos::of);
    }

    @Override
    public LList getLights_() {
        LList list = null;
        int minX = this.chunkPos.getMinBlockX();
        int minZ = this.chunkPos.getMinBlockZ();
        for (LevelChunkSection section : this.sections) {
            if (section == null || section.hasOnlyAir()) {
                continue;
            }
            int startY = section.bottomBlockY();
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (state.getLightEmission() != 0) {
                            if (list == null) {
                                list = new LArrayList();
                            }
                            list.add(BlockPos.asLong(minX + x, startY + y, minZ + z));
                        }
                    }
                }
            }
        }
        if (list == null) {
            return LList.emptyList();
        }
        return list;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean isTicking(BlockPos pos) {
        if (!this.level.getWorldBorder().isWithinBounds_(pos.getX(), pos.getZ())) {
            return false;
        }
        if (!(this.level instanceof ServerLevel level)) {
            return true;
        }
        return this.getFullStatus().isOrAfter(net.minecraft.server.level.ChunkHolder.FullChunkStatus.TICKING) &&
               level.areEntitiesLoaded(ChunkPos.asLong(pos));
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void postProcessGeneration() {
        ChunkPos chunkPos = this.getPos();
        ShortList[] postProcessing = this.postProcessing;
        for (int i = 0; i < postProcessing.length; ++i) {
            ShortList shorts = postProcessing[i];
            if (shorts != null) {
                for (int j = 0, len2 = shorts.size(); j < len2; ++j) {
                    short s = shorts.getShort(j);
                    int x = SectionPos.sectionToBlockCoord(chunkPos.x, s & 15);
                    int y = SectionPos.sectionToBlockCoord(this.getSectionYFromSectionIndex(i), s >>> 4 & 15);
                    int z = SectionPos.sectionToBlockCoord(chunkPos.z, s >>> 8 & 15);
                    BlockState blockState = this.getBlockState_(x, y, z);
                    FluidState fluidState = blockState.getFluidState();
                    if (!fluidState.isEmpty()) {
                        fluidState.tick_(this.level, x, y, z);
                    }
                    if (!(blockState.getBlock() instanceof LiquidBlock)) {
                        BlockState blockState2 = BlockUtils.updateFromNeighbourShapes(blockState, this.level, x, y, z);
                        this.level.setBlock_(x, y, z, blockState2, BlockFlags.NO_RERENDER | BlockFlags.UPDATE_NEIGHBORS);
                    }
                }
                shorts.clear();
            }
        }
        L2OMap<CompoundTag> pendingBlockEntities = this.pendingBlockEntities_();
        for (long it = pendingBlockEntities.beginIteration(); pendingBlockEntities.hasNextIteration(it); it = pendingBlockEntities.nextEntry(it)) {
            long pos = pendingBlockEntities.getIterationKey(it);
            this.getBlockEntity_(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos));
        }
        pendingBlockEntities.clear();
    }

    /**
     * First, we iterate over the "populated part" of the chunk, that is, the bottom part which is filled with non-air blocks, starting from the
     * top-most air block. For now, we're only working within the bounds of the chunk, from top to bottom. Every non-air block will have its atm
     * value set to 31. Air blocks that are not connected to other air blocks above will also be set to 31, but will request an update. We also
     * record the deepest we could reach with atm value 0.
     */
    @Override
    @Unique
    public void primeAtm(boolean needsResetting) {
        LevelChunkSection[] sections = this.sections;
        if (needsResetting) {
            for (LevelChunkSection section : sections) {
                section.getAtmStorage().reset();
            }
        }
        Heightmap heightmap = this.heightmaps_().get(Heightmap.Types.WORLD_SURFACE);
        IList toUpdate = TO_UPDATE.get();
        assert toUpdate.isEmpty();
        int deepestY = Integer.MAX_VALUE;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int maxY = heightmap.getHighestTaken(x, z);
                if (deepestY > maxY + 1) {
                    deepestY = maxY + 1;
                }
                int atm = 0;
                for (int y = maxY; y >= this.getMinBuildHeight(); y--) {
                    int index = this.getSectionIndex(y);
                    if (index < 0 || index >= sections.length) {
                        break;
                    }
                    int localY = y & 15;
                    LevelChunkSection section = sections[index];
                    BlockState state = section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x, localY, z);
                    if (state.isAir()) {
                        if (atm != 0) {
                            section.getAtmStorage().set(x, localY, z, 31);
                            toUpdate.add(IAir.packInternalPos(x, y, z));
                        }
                    }
                    else if (state.getBlock() instanceof IAir air) {
                        if (air.allowsFrom(state, Direction.UP)) {
                            if (atm != 0) {
                                section.getAtmStorage().set(x, localY, z, 31);
                                toUpdate.add(IAir.packInternalPos(x, y, z));
                            }
                            else if (!air.allowsFrom(state, Direction.DOWN)) {
                                atm = 31;
                            }
                        }
                        else {
                            atm = 31;
                            section.getAtmStorage().set(x, localY, z, 31);
                            toUpdate.add(IAir.packInternalPos(x, y, z));
                        }
                    }
                    else {
                        atm = 31;
                        section.getAtmStorage().set(x, localY, z, 31);
                    }
                    if (atm == 0 && deepestY > y) {
                        deepestY = y;
                    }
                }
            }
        }
        this.updateShallowAtm(deepestY, toUpdate);
    }

    @Override
    @Unique
    public void primeStructural(boolean needsResetting) {
        if (needsResetting) {
            LevelChunkSection[] sections = this.sections;
            for (LevelChunkSection section : sections) {
                section.getIntegrityStorage().reset();
                section.getLoadFactorStorage().reset();
                section.getStabilityStorage().reset();
            }
        }
        this.fillPillarsFromStable();
    }

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Overwrite
    public void registerAllBlockEntitiesAfterLevelLoad() {
        L2OMap<BlockEntity> blockEntities = this.blockEntities_();
        for (long it = blockEntities.beginIteration(); blockEntities.hasNextIteration(it); it = blockEntities.nextEntry(it)) {
            BlockEntity te = blockEntities.getIterationValue(it);
            this.addGameEventListener(te);
            this.updateBlockEntityTicker(te);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @SuppressWarnings("removal")
    @Overwrite
    @Override
    public void removeBlockEntity(BlockPos pos) {
        Evolution.deprecatedMethod();
        this.removeBlockEntity_(pos.asLong());
    }

    @Override
    public void removeBlockEntity_(long pos) {
        if (this.isInLevel()) {
            BlockEntity te = this.blockEntities_().remove(pos);
            if (te != null) {
                this.removeGameEventListener(te);
                te.setRemoved();
            }
        }
        this.removeBlockEntityTicker_(pos);
    }

    @Override
    public void replaceWithPacketData_(FriendlyByteBuf buf, CompoundTag tag, Consumer<IBlockEntityTagOutput> consumer) {
        this.clearAllBlockEntities();
        for (LevelChunkSection section : this.sections) {
            section.read(buf);
        }
        for (Heightmap.Types types : ArrayHelper.HEIGHTMAP) {
            String string = types.getSerializationKey();
            if (tag.contains(string, Tag.TAG_LONG_ARRAY)) {
                this.setHeightmap(types, tag.getLongArray(string));
            }
        }
        consumer.accept((x, y, z, type, t) -> {
            BlockEntity blockEntity = this.getBlockEntity_(x, y, z, LevelChunk.EntityCreationType.IMMEDIATE);
            if (blockEntity != null && t != null && blockEntity.getType() == type) {
                blockEntity.load(t);
            }
        });
    }

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Overwrite
    @Override
    public void setBlockEntity(BlockEntity te) {
        BlockPos pos = te.getBlockPos();
        if (this.getBlockState_(pos.getX(), pos.getY(), pos.getZ()).hasBlockEntity()) {
            te.setLevel(this.level);
            te.clearRemoved();
            BlockEntity oldTe = this.blockEntities_().put(pos.asLong(), te);
            if (oldTe != null && oldTe != te) {
                oldTe.setRemoved();
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @SuppressWarnings("removal")
    @Override
    @Overwrite
    public @Nullable BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        Evolution.deprecatedMethod();
        return this.setBlockState_(pos.getX(), pos.getY(), pos.getZ(), state, isMoving);
    }

    @Override
    public @Nullable BlockState setBlockState_(int x, int y, int z, BlockState state, boolean isMoving) {
        LevelChunkSection section = this.getSection(this.getSectionIndex(y));
        boolean hadOnlyAir = section.hasOnlyAir();
        if (hadOnlyAir && state.isAir()) {
            return null;
        }
        int localX = x & 15;
        int localY = y & 15;
        int localZ = z & 15;
        BlockState oldState = section.setBlockState(localX, localY, localZ, state);
        if (oldState == state) {
            return null;
        }
        Block newBlock = state.getBlock();
        R2OMap<Heightmap.Types, Heightmap> heightmaps = this.heightmaps_();
        heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(localX, y, localZ, state);
        heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(localX, y, localZ, state);
        heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(localX, y, localZ, state);
        heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(localX, y, localZ, state);
        boolean hasOnlyAir = section.hasOnlyAir();
        if (hadOnlyAir != hasOnlyAir) {
            this.level.getChunkSource().getLightEngine().updateSectionStatus_block(x, y, z, hasOnlyAir);
        }
        boolean hadTE = oldState.hasBlockEntity();
        if (!this.level.isClientSide) {
            oldState.onRemove_(this.level, x, y, z, state, isMoving);
        }
        else if ((!oldState.is(newBlock) || !state.hasBlockEntity()) && hadTE) {
            this.removeBlockEntity_(BlockPos.asLong(x, y, z));
        }
        if (!section.getBlockState(localX, localY, localZ).is(newBlock)) {
            //IDK when this could happen
            return null;
        }
        if (!this.level.isClientSide) {
            state.onPlace_(this.level, x, y, z, oldState, isMoving);
        }
        if (state.hasBlockEntity()) {
            BlockEntity tile = this.getBlockEntity_(x, y, z, LevelChunk.EntityCreationType.CHECK);
            if (tile == null) {
                //Allocation here is fine
                tile = ((EntityBlock) newBlock).newBlockEntity(new BlockPos(x, y, z), state);
                if (tile != null) {
                    this.addAndRegisterBlockEntity(tile);
                }
            }
            else {
                tile.setBlockState(state);
                this.updateBlockEntityTicker(tile);
            }
        }
        if (!this.level.isClientSide) {
            if (!(newBlock instanceof BlockAtm)) {
                this.chunkStorage.scheduleAtmTick((LevelChunk) (Object) this, localX, y, localZ, newBlock instanceof IAir || oldState.getBlock() instanceof IAir);
            }
            this.chunkStorage.scheduleIntegrityTick((LevelChunk) (Object) this, localX, y, localZ, oldState.getBlock() instanceof IFillable);
        }
        this.unsaved = true;
        return oldState;
    }

    @Shadow
    public abstract void setClientLightReady(boolean bl);

    @Shadow
    protected abstract <T extends BlockEntity> void addGameEventListener(T blockEntity);

    @Shadow
    protected abstract <T extends BlockEntity> TickingBlockEntity createTicker(T blockEntity,
                                                                               BlockEntityTicker<T> blockEntityTicker);

    @Shadow
    protected abstract boolean isInLevel();

    @Shadow
    protected abstract <T extends BlockEntity> void removeGameEventListener(T blockEntity);

    @Unique
    private void breakBlockUpdateLightAndHeightmap(LevelChunkSection section, int x, int localY, int z, int globalY) {
        BlockState state = Blocks.AIR.defaultBlockState();
        section.setBlockState(x, localY, z, state);
        this.level.getLightEngine().checkBlock_(BlockPos.asLong(x + this.chunkPos.getMinBlockX(), globalY, z + this.chunkPos.getMinBlockZ()));
        R2OMap<Heightmap.Types, Heightmap> heightmaps = this.heightmaps_();
        heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(x, globalY, z, state);
        heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(x, globalY, z, state);
        heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(x, globalY, z, state);
        heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(x, globalY, z, state);
    }

    @Unique
    private void breakColumnAndReset(int x, int y, int z) {
        while (true) {
            int index = this.getSectionIndex(y);
            if (index >= this.sections.length) {
                break;
            }
            int localY = y & 15;
            LevelChunkSection section = this.sections[index];
            BlockState state = section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x, localY, z);
            Block block = state.getBlock();
            if (block instanceof IStable) {
                break;
            }
            if (block instanceof IFillable) {
                this.breakBlockUpdateLightAndHeightmap(section, x, localY, z, y);
                section.getIntegrityStorage().set(x, localY, z, 0);
                section.getLoadFactorStorage().set(x, localY, z, 0);
            }
            else {
                break;
            }
            ++y;
        }
    }

    @Unique
    private boolean calculateBeams(IList potentialBeams, ISet confirmedBeams, ChunkHolder holder) {
        boolean changed = false;
        int len = potentialBeams.size();
        for (int i = 0; i < len; ++i) {
            int packed = potentialBeams.getInt(i);
            int x = IFillable.unpackX(packed);
            int y = IFillable.unpackY(packed);
            int z = IFillable.unpackZ(packed);
            int index = this.getSectionIndex(y);
            if (index < 0 || index >= this.sections.length) {
                continue;
            }
            int localY = y & 15;
            LevelChunkSection section = this.sections[index];
            BlockState stateBelow = CapabilityChunkStorage.safeGetBlockstate((LevelChunk) (Object) this, section, holder, x, localY - 1, z, index, 0);
            if (stateBelow.getBlock() instanceof IFillable) {
                Evolution.info("This shouldn't happen!");
                continue;
            }
            BlockState state = section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x, localY, z);
            if (state.getBlock() instanceof IStructural structural) {
                int selfIntegrity = structural.getIntegrity(state);
                int incrementForBeam = structural.getIncrementForBeam(state);
                int result = switch (structural.getBeamType(state)) {
                    case NONE -> {
                        //Try to get a beam upwards
                        this.breakBlockUpdateLightAndHeightmap(section, x, localY, z, y);
                        BlockState stateAbove = CapabilityChunkStorage.safeGetBlockstate((LevelChunk) (Object) this, section, holder, x, localY + 1, z, index, 0);
                        if (stateAbove.getBlock() instanceof IFillable) {
                            changed = true;
                            potentialBeams.add(IFillable.packInternalPos(x, y + 1, z, 0, PropagationDirection.NONE, false));
                        }
                        yield 0;
                    }
                    case X_BEAM -> CapabilityChunkStorage.verifySimpleBeam(state, structural, (LevelChunk) (Object) this, section, holder, DirectionList.X_AXIS, index, x, localY, z, incrementForBeam, selfIntegrity, 255, selfIntegrity);
                    case Z_BEAM -> CapabilityChunkStorage.verifySimpleBeam(state, structural, (LevelChunk) (Object) this, section, holder, DirectionList.Z_AXIS, index, x, localY, z, incrementForBeam, selfIntegrity, 255, selfIntegrity);
                    case CARDINAL_BEAM -> CapabilityChunkStorage.verifySimpleBeam(state, structural, (LevelChunk) (Object) this, section, holder, DirectionList.HORIZONTAL, index, x, localY, z, incrementForBeam, selfIntegrity, 255, selfIntegrity);
                    case X_ARCH -> CapabilityChunkStorage.verifyArch(state, structural, (LevelChunk) (Object) this, section, holder, index, x, localY, z, selfIntegrity, DirectionList.X_AXIS, incrementForBeam, structural.getIncrementForArch(state));
                    case Z_ARCH -> CapabilityChunkStorage.verifyArch(state, structural, (LevelChunk) (Object) this, section, holder, index, x, localY, z, selfIntegrity, DirectionList.Z_AXIS, incrementForBeam, structural.getIncrementForArch(state));
                    case CARDINAL_ARCH -> CapabilityChunkStorage.verifyArch(state, structural, (LevelChunk) (Object) this, section, holder, index, x, localY, z, selfIntegrity, DirectionList.HORIZONTAL, incrementForBeam, structural.getIncrementForArch(state));
                };
                if (result == 0) {
                    continue;
                }
                int newLoad = result >> 16;
                int newIntegrity = result & 0xFFFF;
                if (newLoad != 255 && newLoad <= newIntegrity) {
                    changed = true;
                    section.getIntegrityStorage().set(x, localY, z, newIntegrity);
                    section.getLoadFactorStorage().set(x, localY, z, newLoad);
                    boolean newStable = switch (structural.getStabilization(state)) {
                        case BEAM -> CapabilityChunkStorage.canStabilize((LevelChunk) (Object) this, section, holder, structural, state, x, localY, z, index, newLoad);
                        case ARCH -> CapabilityChunkStorage.canStabilizeArch((LevelChunk) (Object) this, section, holder, structural, state, x, localY, z, index, newLoad);
                        case NONE -> false;
                    };
                    section.getStabilityStorage().set(x, localY, z, newStable);
                    confirmedBeams.add(packed);
                }
                else {
                    potentialBeams.add(packed);
                }
            }
            else if (state.getBlock() instanceof IFillable) {
                //Try to get a beam upwards
                this.breakBlockUpdateLightAndHeightmap(section, x, localY, z, y);
                BlockState stateAbove = CapabilityChunkStorage.safeGetBlockstate((LevelChunk) (Object) this, section, holder, x, localY + 1, z, index, 0);
                if (stateAbove.getBlock() instanceof IFillable) {
                    potentialBeams.add(IFillable.packInternalPos(x, y + 1, z, 0, PropagationDirection.NONE, false));
                    changed = true;
                }
            }
        }
        potentialBeams.removeElements(0, len);
        return changed;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private @Nullable BlockEntity createBlockEntity(BlockPos pos) {
        throw new AbstractMethodError();
    }

    @Unique
    private @Nullable BlockEntity createBlockEntity_(int x, int y, int z) {
        BlockState state = this.getBlockState_(x, y, z);
        //It's fine to allocate here, since this BlockPos will be saved to the BlockEntity itself
        return !state.hasBlockEntity() ? null : ((EntityBlock) state.getBlock()).newBlockEntity(new BlockPos(x, y, z), state);
    }

    @Unique
    private void fillPillarsFromStable() {
        IList potentialBeams = TO_UPDATE.get();
        potentialBeams.clear();
//        assert potentialBeams.isEmpty();
        ISet confirmedBeams = AUX_SET.get();
        confirmedBeams.clear();
//        assert confirmedBeams.isEmpty();
        LevelChunkSection[] sections = this.sections;
        Heightmap heightmap = this.heightmaps_().get(Heightmap.Types.WORLD_SURFACE);
        int dependencies = 0;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int integrity = 0;
                boolean belowEmpty = true;
                for (int y = this.getMinBuildHeight(), maxBuildHeight = heightmap.getFirstAvailable(x, z); y < maxBuildHeight; ++y) {
                    int index = this.getSectionIndex(y);
                    if (index < 0 || index >= sections.length) {
                        break;
                    }
                    int localY = y & 15;
                    LevelChunkSection section = sections[index];
                    BlockState state = section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x, localY, z);
                    Block block = state.getBlock();
                    if (block instanceof IStable) {
                        //Load factor should be already set to 0
                        section.getIntegrityStorage().set(x, localY, z, 255);
                        integrity = 255;
                        belowEmpty = false;
                    }
                    else if (block instanceof IStructural structural) {
                        if (integrity != 0) {
                            integrity = Math.min(integrity, structural.getIntegrity(state));
                            //Load factor should be already set to 0
                            section.getIntegrityStorage().set(x, localY, z, integrity);
                        }
                        else {
                            if (belowEmpty) {
                                potentialBeams.add(IFillable.packInternalPos(x, y, z, 0, PropagationDirection.NONE, false));
                                if (x == 0) {
                                    dependencies |= 1 << DirectionUtil.horizontalIndex(Direction.WEST);
                                }
                                else if (x == 15) {
                                    dependencies |= 1 << DirectionUtil.horizontalIndex(Direction.EAST);
                                }
                                if (z == 0) {
                                    dependencies |= 1 << DirectionUtil.horizontalIndex(Direction.NORTH);
                                }
                                else if (z == 15) {
                                    dependencies |= 1 << DirectionUtil.horizontalIndex(Direction.SOUTH);
                                }
                            }
                        }
                        belowEmpty = false;
                    }
                    else if (block instanceof IFillable) {
                        if (integrity != 0) {
                            //Load factor should be already set to 0
                            section.getIntegrityStorage().set(x, localY, z, integrity);
                        }
                        else {
                            if (belowEmpty) {
                                potentialBeams.add(IFillable.packInternalPos(x, y, z, 0, PropagationDirection.NONE, false));
                                if (x == 0) {
                                    dependencies |= 1 << DirectionUtil.horizontalIndex(Direction.WEST);
                                }
                                else if (x == 15) {
                                    dependencies |= 1 << DirectionUtil.horizontalIndex(Direction.EAST);
                                }
                                if (z == 0) {
                                    dependencies |= 1 << DirectionUtil.horizontalIndex(Direction.NORTH);
                                }
                                else if (z == 15) {
                                    dependencies |= 1 << DirectionUtil.horizontalIndex(Direction.SOUTH);
                                }
                            }
                        }
                        belowEmpty = false;
                    }
                    else {
                        integrity = 0;
                        belowEmpty = true;
                    }
                }
            }
        }
        Evolution.info("Dependencies = {}", Integer.toBinaryString(dependencies));
        this.unsaved = true;
        this.propagateBeams(potentialBeams, confirmedBeams, dependencies);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private @Nullable BlockEntity promotePendingBlockEntity(BlockPos pos, CompoundTag tag) {
        throw new AbstractMethodError();
    }

    @Unique
    private @Nullable BlockEntity promotePendingBlockEntity_(int x, int y, int z, CompoundTag tag) {
        BlockState state = this.getBlockState_(x, y, z);
        BlockEntity blockEntity;
        if ("DUMMY".equals(tag.getString("id"))) {
            if (state.hasBlockEntity()) {
                //It's fine to allocate here, since this BlockPos will be saved to the BlockEntity itself
                blockEntity = ((EntityBlock) state.getBlock()).newBlockEntity(new BlockPos(x, y, z), state);
            }
            else {
                blockEntity = null;
                LOGGER.warn("Tried to load a DUMMY block entity at [{}, {}, {}] but found not block entity block {} at location", x, y, z, state);
            }
        }
        else {
            blockEntity = TEUtils.loadStatic(x, y, z, state, tag);
        }
        if (blockEntity != null) {
            blockEntity.setLevel(this.level);
            this.addAndRegisterBlockEntity(blockEntity);
        }
        else {
            LOGGER.warn("Tried to load a block entity for block {} but failed at location [{}, {}, {}]", state, x, y, z);
        }
        return blockEntity;
    }

    @Unique
    private void propagateBeams(IList potentialBeams, ISet confirmedBeams, int dependencies) {
        if (!potentialBeams.isEmpty() && dependencies == 0) {
            Evolution.info("Chunk at {} has structure and no dependencies!", this.chunkPos);
        }
        ChunkHolder holder = HOLDER.get();
        holder.reset();
        while (true) {
            boolean changed = true;
            while (!potentialBeams.isEmpty()) {
                if (!changed) {
//                    if (this.tryToAdjustBeams(confirmedBeams, holder)) {
//                        if (this.calculateBeams(potentialBeams, confirmedBeams, holder)) {
//                            changed = true;
//                            continue;
//                        }
//                    }
                    break;
                }
                changed = this.calculateBeams(potentialBeams, confirmedBeams, holder);
            }
            if (dependencies == 0) {
                if (potentialBeams.isEmpty()) {
//                    this.propagateWeights(confirmedBeams);
                    this.warnNeighbours();
                    this.unsaved = true;
                    return;
                }
                //Transform beams upwards
                int len = potentialBeams.size();
                int minY = Integer.MAX_VALUE;
                int pos = -1;
                int packedMinY = 0;
                for (int i = 0; i < len; ++i) {
                    int packed = potentialBeams.getInt(i);
                    int y = IFillable.unpackY(packed);
                    if (y < minY) {
                        minY = y;
                        pos = i;
                        packedMinY = packed;
                    }
                }
                int x = IFillable.unpackX(packedMinY);
                int z = IFillable.unpackZ(packedMinY);
                int index = this.getSectionIndex(minY);
                assert !(index < 0 || index >= this.sections.length);
                int localY = minY & 15;
                LevelChunkSection section = this.sections[index];
//                this.breakBlockUpdateLightAndHeightmap(section, x, localY, z, minY);
                BlockState stateAbove = CapabilityChunkStorage.safeGetBlockstate((LevelChunk) (Object) this, section, holder, x, localY + 1, z, index, 0);
                if (stateAbove.getBlock() instanceof IFillable) {
                    potentialBeams.set(pos, IFillable.packInternalPos(x, minY + 1, z, 0, PropagationDirection.NONE, false));
                }
                continue;
            }
            //We have dependencies
            this.storeDependencies(potentialBeams, confirmedBeams, dependencies);
            this.warnNeighbours();
            this.unsaved = true;
            return;
        }
    }

    @Unique
    private void propagateWeights(ISet confirmedBeams) {
        for (long it = confirmedBeams.beginIteration(); confirmedBeams.hasNextIteration(it); it = confirmedBeams.nextEntry(it)) {
            int packed = confirmedBeams.getIteration(it);
            int x = IFillable.unpackX(packed);
            int originY = IFillable.unpackY(packed);
            int z = IFillable.unpackZ(packed);
            int oldIndex = this.getSectionIndex(originY);
            assert !(oldIndex < 0 || oldIndex >= this.sections.length) : "Bad index, how did it get here?";
            int localY = originY & 15;
            LevelChunkSection oldSection = this.sections[oldIndex];
            assert oldSection.getBlockState(x, localY, z).getBlock() instanceof IStructural;
            int integrity = oldSection.getIntegrityStorage().get(x, localY, z);
            int load = oldSection.getLoadFactorStorage().get(x, localY, z);
            boolean stable = oldSection.getStabilityStorage().get(x, localY, z);
            int y = originY;
            int failY = originY + 1;
            while (true) {
                ++y;
                int index = this.getSectionIndex(y);
                LevelChunkSection section;
                if (index == oldIndex) {
                    section = oldSection;
                }
                else {
                    if (index >= this.sections.length) {
                        break;
                    }
                    section = this.sections[index];
                    oldIndex = index;
                    oldSection = section;
                }
                localY = y & 15;
                BlockState state = section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x, localY, z);
                Block block = state.getBlock();
                if (block instanceof IStable) {
                    break;
                }
                if (block instanceof IStructural structural) {
                    int newIntegrity = structural.getIntegrity(state);
                    if (newIntegrity < integrity) {
                        integrity = newIntegrity;
                        failY = y;
                    }
                    if (stable) {
                        load = 0;
                    }
                    else {
                        ++load;
                        if (load > integrity) {
                            this.breakColumnAndReset(x, failY, z);
                            break;
                        }
                    }
                    section.getIntegrityStorage().set(x, localY, z, integrity);
                    section.getLoadFactorStorage().set(x, localY, z, load);
                }
                else if (block instanceof IFillable) {
                    if (stable) {
                        load = 0;
                    }
                    else {
                        ++load;
                        if (load > integrity) {
                            this.breakColumnAndReset(x, failY, z);
                            break;
                        }
                    }
                    section.getIntegrityStorage().set(x, localY, z, integrity);
                    section.getLoadFactorStorage().set(x, localY, z, load);
                }
                else {
                    break;
                }
            }
        }
        confirmedBeams.clear();
    }

    @Unique
    private void removeBlockEntityTicker_(long pos) {
        LevelChunk.RebindableTickingBlockEntityWrapper w = this.tickersInLevel_.remove(pos);
        if (w != null) {
            w.rebind(NULL_TICKER);
        }
    }

    @Unique
    private void storeDependencies(IList potentialBeams, ISet confirmedBeams, int dependencies) {
        potentialBeams.clear();
        confirmedBeams.clear();
        //TODO
    }

    @Unique
    private boolean tryToAdjustBeams(ISet confirmedBeams, ChunkHolder holder) {
        boolean changedEver = false;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (long it = confirmedBeams.beginIteration(); confirmedBeams.hasNextIteration(it); it = confirmedBeams.nextEntry(it)) {
                int packed = confirmedBeams.getIteration(it);
                int x = IFillable.unpackX(packed);
                int y = IFillable.unpackY(packed);
                int z = IFillable.unpackZ(packed);
                int index = this.getSectionIndex(y);
                assert !(index < 0 || index >= this.sections.length) : "Bad index, how did it get here?";
                int localY = y & 15;
                LevelChunkSection section = this.sections[index];
                int oldIntegrity = section.getIntegrityStorage().get(x, localY, z);
                int oldLoad = section.getLoadFactorStorage().get(x, localY, z);
                boolean oldStable = section.getStabilityStorage().get(x, localY, z);
                BlockState state = section.getBlockState(x, localY, z);
                assert state.getBlock() instanceof IStructural : "How did this block form a beam then?";
                IStructural structural = (IStructural) state.getBlock();
                int selfIntegrity = structural.getIntegrity(state);
                int incrementForBeam = structural.getIncrementForBeam(state);
                int newLoad = oldLoad;
                int newIntegrity = oldIntegrity;
                boolean newStable = oldStable;
                int result = switch (structural.getBeamType(state)) {
                    case NONE -> {
                        throw new IllegalStateException("Structural block with no beam type formed a beam!? State = " + state + ", @ = (" + (this.chunkPos.getMinBlockX() + x) + ", " + y + ", " + (this.chunkPos.getMinBlockZ() + z) + ")");
                    }
                    case X_BEAM -> CapabilityChunkStorage.verifySimpleBeam(state, structural, (LevelChunk) (Object) this, section, holder, DirectionList.X_AXIS, index, x, localY, z, incrementForBeam, selfIntegrity, 255, selfIntegrity);
                    case Z_BEAM -> CapabilityChunkStorage.verifySimpleBeam(state, structural, (LevelChunk) (Object) this, section, holder, DirectionList.Z_AXIS, index, x, localY, z, incrementForBeam, selfIntegrity, 255, selfIntegrity);
                    case CARDINAL_BEAM -> CapabilityChunkStorage.verifySimpleBeam(state, structural, (LevelChunk) (Object) this, section, holder, DirectionList.HORIZONTAL, index, x, localY, z, incrementForBeam, selfIntegrity, 255, selfIntegrity);
                    case X_ARCH -> CapabilityChunkStorage.verifyArch(state, structural, (LevelChunk) (Object) this, section, holder, index, x, localY, z, selfIntegrity, DirectionList.X_AXIS, incrementForBeam, structural.getIncrementForArch(state));
                    case Z_ARCH -> CapabilityChunkStorage.verifyArch(state, structural, (LevelChunk) (Object) this, section, holder, index, x, localY, z, selfIntegrity, DirectionList.Z_AXIS, incrementForBeam, structural.getIncrementForArch(state));
                    case CARDINAL_ARCH -> CapabilityChunkStorage.verifyArch(state, structural, (LevelChunk) (Object) this, section, holder, index, x, localY, z, selfIntegrity, DirectionList.HORIZONTAL, incrementForBeam, structural.getIncrementForArch(state));
                };
                newLoad = result >> 16;
                newIntegrity = result & 0xFFFF;
                if (newLoad == 255 || newLoad > newIntegrity) {
                    throw new IllegalStateException("Block destabilized: State = " + state + ", @ = (" + (this.chunkPos.getMinBlockX() + x) + ", " + y + ", " + (this.chunkPos.getMinBlockZ() + z) + ")");
                }
                newStable = switch (structural.getStabilization(state)) {
                    case NONE -> false;
                    case BEAM -> CapabilityChunkStorage.canStabilize((LevelChunk) (Object) this, section, holder, structural, state, x, localY, z, index, newLoad);
                    case ARCH -> CapabilityChunkStorage.canStabilizeArch((LevelChunk) (Object) this, section, holder, structural, state, x, localY, z, index, newLoad);
                };
                if (newLoad != oldLoad) {
                    section.getLoadFactorStorage().set(x, localY, z, newLoad);
                    changed = true;
                    changedEver = true;
                }
                if (newIntegrity != oldIntegrity) {
                    section.getIntegrityStorage().set(x, localY, z, newIntegrity);
                    changed = true;
                    changedEver = true;
                }
                if (newStable != oldStable) {
                    if (oldStable) {
                        Evolution.info("Block was stable and turned not stable!");
                    }
                    section.getStabilityStorage().set(x, localY, z, newStable);
                    changed = true;
                    changedEver = true;
                }
                if (newStable) {
                    //TODO propagate stability upwards and probably remove from set
                }
            }
        }
        return changedEver;
    }

    /**
     * The last part of the Atm Priming. Here, we will propagate all the pending updates within this chunk.
     */
    @Unique
    private void updateAtmFurther(IList toUpdate, ChunkHolder holder) {
        while (!toUpdate.isEmpty()) {
            int len = toUpdate.size();
            for (int i = 0; i < len; ++i) {
                int pos = toUpdate.getInt(i);
                int globalY = IAir.unpackY(pos);
                int sectionIndex = this.getSectionIndex(globalY);
                if (sectionIndex < 0 || sectionIndex >= this.sections.length) {
                    continue;
                }
                LevelChunkSection section = this.sections[sectionIndex];
                int x = IAir.unpackX(pos);
                int y = globalY & 15;
                int z = IAir.unpackZ(pos);
                int oldAtm = section.getAtmStorage().get(x, y, z);
                if (oldAtm == 0) {
                    continue;
                }
                BlockState state = section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x, y, z);
                boolean isAir = state.isAir();
                IAir air = null;
                if (!isAir) {
                    if (state.getBlock() instanceof IAir a) {
                        air = a;
                    }
                    else {
                        continue;
                    }
                }
                int lowestAtm = oldAtm;
                Direction lowest = null;
                int list = 0;
                for (Direction dir : DirectionUtil.ALL) {
                    if (!isAir && !air.allowsFrom(state, dir)) {
                        continue;
                    }
                    int x1 = x + dir.getStepX();
                    int y1 = y + dir.getStepY();
                    int z1 = z + dir.getStepZ();
                    BlockState stateAtDir = CapabilityChunkStorage.safeGetBlockstate((LevelChunk) (Object) this, section, holder, x1, y1, z1, sectionIndex, 0);
                    if (stateAtDir.isAir() || stateAtDir.getBlock() instanceof IAir a && a.allowsFrom(stateAtDir, dir.getOpposite())) {
                        list = DirectionList.add(list, dir);
                        int atm = CapabilityChunkStorage.safeGetAtm((LevelChunk) (Object) this, section, holder, x1, y1, z1, sectionIndex);
                        if (isAir) {
                            ++atm;
                        }
                        else {
                            atm += air.increment(state, dir);
                        }
                        if (lowestAtm > atm) {
                            lowest = dir;
                            lowestAtm = atm;
                        }
                    }
                }
                if (lowestAtm < oldAtm) {
                    section.getAtmStorage().set(x, y, z, lowestAtm);
                    while (!DirectionList.isEmpty(list)) {
                        int index = DirectionList.getLast(list);
                        Direction dir = DirectionList.get(list, index);
                        list = DirectionList.remove(list, index);
                        if (dir == lowest) {
                            continue;
                        }
                        int x1 = x + dir.getStepX();
                        int z1 = z + dir.getStepZ();
                        if (0 <= x1 && x1 < 16 && 0 <= z1 && z1 < 16) {
                            toUpdate.add(IAir.packInternalPos(x1, globalY + dir.getStepY(), z1));
                        }
                        else {
                            LevelChunk chunk = holder.getHeld(dir);
                            //noinspection ObjectAllocationInLoop
                            assert chunk != null : "Chunk at " + dir + " is null, how did you access it in the first place, then?";
                            chunk.getChunkStorage().scheduleAtmTick(chunk, x1, globalY, z1, false);
                        }
                    }
                }
            }
            toUpdate.removeElements(0, len);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Overwrite
    private <T extends BlockEntity> void updateBlockEntityTicker(T te) {
        BlockState state = te.getBlockState();
        BlockEntityTicker<T> ticker = (BlockEntityTicker<T>) state.getTicker(this.level, te.getType());
        if (ticker == null) {
            this.removeBlockEntityTicker_(te.getBlockPos().asLong());
        }
        else {
            long pos = te.getBlockPos().asLong();
            LevelChunk.RebindableTickingBlockEntityWrapper old = this.tickersInLevel_.get(pos);
            TickingBlockEntity newTicker = this.createTicker(te, ticker);
            if (old != null) {
                old.rebind(newTicker);
            }
            else if (this.isInLevel()) {
                TickingBlockEntity newNewTicker = ((LevelChunk) (Object) this).new RebindableTickingBlockEntityWrapper(newTicker);
                this.level.addBlockEntityTicker(newNewTicker);
            }
        }
    }

    /**
     * Here, we iterate over the list of positions that requested an update on {@link PatchLevelChunk#primeAtm(boolean)}. Positions that are internal to
     * the chunk, that is, that are deeper than the deepest we could reach with atm 0 and are not at the boundaries of the chunk, will be discarded.
     * Air blocks on updating positions will verify all their 6 neighbours looking for the lowest atm possible, given incrementation and allowing
     * rules. If they manage to find a lower atm value, they will update to that value and request that their neighbouring air blocks schedule an
     * update.
     */
    @Unique
    private void updateShallowAtm(int deepestY, IList toUpdate) {
        ChunkHolder holder = HOLDER.get();
        holder.reset();
        int len = toUpdate.size();
        for (int i = 0; i < len; ++i) {
            int pos = toUpdate.getInt(i);
            int x = IAir.unpackX(pos);
            int globalY = IAir.unpackY(pos);
            int z = IAir.unpackZ(pos);
            if (globalY < deepestY) {
                if (x != 0 && x != 15 && z != 0 && z != 15) {
                    continue;
                }
            }
            int sectionIndex = this.getSectionIndex(globalY);
            if (sectionIndex < 0 || sectionIndex >= this.sections.length) {
                continue;
            }
            LevelChunkSection section = this.sections[sectionIndex];
            int y = globalY & 15;
            BlockState state = section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x, y, z);
            boolean isAir = state.isAir();
            IAir air = null;
            if (!isAir) {
                if (state.getBlock() instanceof IAir a) {
                    air = a;
                }
                else {
                    continue;
                }
            }
            int lowestAtm = 31;
            int list = 0;
            Direction lowest = null;
            for (Direction dir : DirectionUtil.ALL) {
                if (!isAir && !air.allowsFrom(state, dir)) {
                    continue;
                }
                int x1 = x + dir.getStepX();
                int y1 = y + dir.getStepY();
                int z1 = z + dir.getStepZ();
                BlockState stateAtDir = CapabilityChunkStorage.safeGetBlockstate((LevelChunk) (Object) this, section, holder, x1, y1, z1, sectionIndex, 0);
                if (stateAtDir.isAir() || stateAtDir.getBlock() instanceof IAir a && a.allowsFrom(stateAtDir, dir.getOpposite())) {
                    list = DirectionList.add(list, dir);
                    int atm = CapabilityChunkStorage.safeGetAtm((LevelChunk) (Object) this, section, holder, x1, y1, z1, sectionIndex);
                    if (isAir) {
                        ++atm;
                    }
                    else {
                        atm += air.increment(state, dir);
                    }
                    if (lowestAtm > atm) {
                        lowest = dir;
                        lowestAtm = atm;
                    }
                }
            }
            if (lowestAtm < 31) {
                section.getAtmStorage().set(x, y, z, lowestAtm);
                while (!DirectionList.isEmpty(list)) {
                    int index = DirectionList.getLast(list);
                    Direction dir = DirectionList.get(list, index);
                    list = DirectionList.remove(list, index);
                    if (dir == lowest) {
                        continue;
                    }
                    int x1 = x + dir.getStepX();
                    int z1 = z + dir.getStepZ();
                    if (0 <= x1 && x1 < 16 && 0 <= z1 && z1 < 16) {
                        toUpdate.add(IAir.packInternalPos(x1, globalY + dir.getStepY(), z1));
                    }
                    else {
                        LevelChunk chunk = holder.getHeld(dir);
                        //noinspection ObjectAllocationInLoop
                        assert chunk != null : "Chunk at " + dir + " is null, how did you access it in the first place, then?";
                        chunk.getChunkStorage().scheduleAtmTick(chunk, x1, globalY, z1, false);
                    }
                }
            }
        }
        toUpdate.removeElements(0, len);
        this.updateAtmFurther(toUpdate, holder);
    }

    @Unique
    private void warnNeighbours() {
        //TODO
    }
}
