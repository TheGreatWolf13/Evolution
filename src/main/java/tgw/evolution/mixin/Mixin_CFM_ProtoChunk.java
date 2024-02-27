package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.*;
import tgw.evolution.util.collection.ArrayHelper;
import tgw.evolution.util.collection.lists.LArrayList;
import tgw.evolution.util.collection.lists.LList;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.sets.RSet;
import tgw.evolution.util.collection.sets.SimpleEnumSet;
import tgw.evolution.world.lighting.StarLightEngine;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(ProtoChunk.class)
public abstract class Mixin_CFM_ProtoChunk extends ChunkAccess {

    @Unique private final LList lights_;
    @Mutable @Shadow @Final @RestoreFinal private ProtoChunkTicks<Block> blockTicks;
    @Mutable @Shadow @Final @RestoreFinal private Map<GenerationStep.Carving, CarvingMask> carvingMasks;
    @Mutable @Shadow @Final @RestoreFinal private List<CompoundTag> entities;
    @Mutable @Shadow @Final @RestoreFinal private ProtoChunkTicks<Fluid> fluidTicks;
    @Shadow private volatile @Nullable LevelLightEngine lightEngine;
    @Shadow @Final @DeleteField private List<BlockPos> lights;
    @Shadow private volatile ChunkStatus status;

    @DummyConstructor
    public Mixin_CFM_ProtoChunk(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, long l, @Nullable LevelChunkSection[] levelChunkSections, @Nullable BlendingData blendingData, LList lights_) {
        super(chunkPos, upgradeData, levelHeightAccessor, registry, l, levelChunkSections, blendingData);
        this.lights_ = lights_;
    }

    @ModifyConstructor
    public Mixin_CFM_ProtoChunk(ChunkPos chunkPos,
                                UpgradeData upgradeData,
                                @Nullable LevelChunkSection[] levelChunkSections,
                                ProtoChunkTicks<Block> protoChunkTicks,
                                ProtoChunkTicks<Fluid> protoChunkTicks2,
                                LevelHeightAccessor levelHeightAccessor,
                                Registry<Biome> registry,
                                @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, registry, 0L, levelChunkSections, blendingData);
        this.status = ChunkStatus.EMPTY;
        this.entities = new OArrayList<>();
        this.lights_ = new LArrayList();
        this.carvingMasks = new EnumMap<>(GenerationStep.Carving.class);
        this.blockTicks = protoChunkTicks;
        this.fluidTicks = protoChunkTicks2;
        //noinspection ConstantValue
        if (!((Object) this instanceof ImposterProtoChunk)) {
            this.setBlockShorts(StarLightEngine.getFilledEmptyLightShort(levelHeightAccessor));
            this.setSkyNibbles(StarLightEngine.getFilledEmptyLightNibble(levelHeightAccessor));
        }
    }

    @Unique
    private static short packOffsetCoordinates_(int x, int y, int z) {
        return (short) (x & 15 | (y & 15) << 4 | (z & 15) << 8);
    }

    @Overwrite
    public void addLight(BlockPos pos) {
        this.lights_.add(pos.asLong());
    }

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Overwrite
    public Map<BlockPos, BlockEntity> getBlockEntities() {
        Evolution.warn("getBlockEntities() should not be called!");
        return Map.of();
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockEntity_(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Override
    @Overwrite
    public @Nullable CompoundTag getBlockEntityNbtForSaving(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockEntityNbtForSaving_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public @Nullable CompoundTag getBlockEntityNbtForSaving_(int x, int y, int z) {
        BlockEntity te = this.getBlockEntity_(x, y, z);
        return te != null ? te.saveWithFullMetadata() : this.pendingBlockEntities_().get(BlockPos.asLong(x, y, z));
    }

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Overwrite
    public Map<BlockPos, CompoundTag> getBlockEntityNbts() {
        Evolution.warn("getBlockEntityNbts() should not be called!");
        return Map.of();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z) {
        return this.blockEntities_().get(BlockPos.asLong(x, y, z));
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public BlockState getBlockState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BlockState getBlockState_(int x, int y, int z) {
        if (this.isOutsideBuildHeight(y)) {
            return Blocks.VOID_AIR.defaultBlockState();
        }
        LevelChunkSection section = this.getSection(this.getSectionIndex(y));
        return section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x & 15, y & 15, z & 15);
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public FluidState getFluidState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getFluidState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public FluidState getFluidState_(int x, int y, int z) {
        if (this.isOutsideBuildHeight(y)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        LevelChunkSection section = this.getSection(this.getSectionIndex(y));
        return section.hasOnlyAir() ? Fluids.EMPTY.defaultFluidState() : section.getFluidState(x & 15, y & 15, z & 15);
    }

    @Override
    @Overwrite
    public Stream<BlockPos> getLights() {
        Evolution.deprecatedMethod();
        return this.lights_.longStream().mapToObj(BlockPos::of);
    }

    @Override
    public LList getLights_() {
        return this.lights_;
    }

    @Overwrite
    public ShortList[] getPackedLights() {
        ShortList[] shortLists = new ShortList[this.getSectionsCount()];
        for (int i = 0, len = this.lights_.size(); i < len; ++i) {
            long pos = this.lights_.getLong(i);
            int y = BlockPos.getY(pos);
            ChunkAccess.getOrCreateOffsetList(shortLists, this.getSectionIndex(y))
                       .add(packOffsetCoordinates_(BlockPos.getX(pos), y, BlockPos.getZ(pos)));
        }
        return shortLists;
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    @DeleteMethod
    public void markPosForPostprocessing(BlockPos pos) {
        throw new AbstractMethodError();
    }

    @Override
    public void markPosForPostprocessing_(int x, int y, int z) {
        if (!this.isOutsideBuildHeight(y)) {
            ChunkAccess.getOrCreateOffsetList(this.postProcessing, this.getSectionIndex(y)).add(packOffsetCoordinates_(x, y, z));
        }
    }

    @Override
    @Overwrite
    public void removeBlockEntity(BlockPos pos) {
        Evolution.deprecatedMethod();
        this.removeBlockEntity_(pos.asLong());
    }

    @Override
    public void removeBlockEntity_(long pos) {
        this.blockEntities_().remove(pos);
        this.pendingBlockEntities_().remove(pos);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Overwrite
    @Override
    public void setBlockEntity(BlockEntity te) {
        this.blockEntities_().put(te.getBlockPos().asLong(), te);
    }

    @Override
    @Overwrite
    public @Nullable BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        Evolution.deprecatedMethod();
        return this.setBlockState_(pos.getX(), pos.getY(), pos.getZ(), state, isMoving);
    }

    @Override
    public @Nullable BlockState setBlockState_(int x, int y, int z, BlockState state, boolean isMoving) {
        if (y >= this.getMinBuildHeight() && y < this.getMaxBuildHeight()) {
            int sectionIndex = this.getSectionIndex(y);
            if (this.sections[sectionIndex].hasOnlyAir() && state.is(Blocks.AIR)) {
                return state;
            }
            if (state.getLightEmission() > 0) {
                this.lights_.add(BlockPos.asLong((x & 15) + this.getPos().getMinBlockX(), y, (z & 15) + this.getPos().getMinBlockZ()));
            }
            LevelChunkSection section = this.getSection(sectionIndex);
            BlockState oldState = section.setBlockState(x & 15, y & 15, z & 15, state);
            if (this.status.isOrAfter(ChunkStatus.FEATURES) &&
                state != oldState &&
                (state.getLightBlock_(this, x, y, z) != oldState.getLightBlock_(this, x, y, z) ||
                 state.getLightEmission() != oldState.getLightEmission() ||
                 state.useShapeForLightOcclusion() ||
                 oldState.useShapeForLightOcclusion())) {
                LevelLightEngine lightEngine = this.lightEngine;
                assert lightEngine != null;
                lightEngine.checkBlock(new BlockPos(x, y, z));
            }
            EnumSet<Heightmap.Types> heightmaps = this.getStatus().heightmapsAfter();
            RSet<Heightmap.Types> toPrime = null;
            for (Heightmap.Types type : ArrayHelper.HEIGHTMAP) {
                if (heightmaps.contains(type)) {
                    Heightmap heightmap = this.heightmaps.get(type);
                    if (heightmap == null) {
                        if (toPrime == null) {
                            toPrime = new SimpleEnumSet<>(Heightmap.Types.class, ArrayHelper.HEIGHTMAP);
                        }
                        toPrime.add(type);
                    }
                }
            }
            if (toPrime != null) {
                Heightmap.primeHeightmaps(this, toPrime);
            }
            for (Heightmap.Types type : ArrayHelper.HEIGHTMAP) {
                if (heightmaps.contains(type)) {
                    this.heightmaps.get(type).update(x & 15, y, z & 15, state);
                }
            }
            return oldState;
        }
        return Blocks.VOID_AIR.defaultBlockState();
    }
}
