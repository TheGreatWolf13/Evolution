package tgw.evolution.mixin;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.TEUtils;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchChunkAccess;
import tgw.evolution.util.collection.maps.L2OHashMap;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;
import tgw.evolution.world.lighting.SWMRNibbleArray;
import tgw.evolution.world.lighting.SWMRShortArray;

import java.util.Map;
import java.util.Set;

@Mixin(ChunkAccess.class)
public abstract class Mixin_CF_ChunkAccess implements PatchChunkAccess {

    @Shadow @Final private static Logger LOGGER;
    @Unique private final L2OMap<BlockEntity> blockEntities_;
    @Unique private final L2OMap<CompoundTag> pendingBlockEntities_;
    @Shadow protected @Nullable BlendingData blendingData;
    @Shadow @Final @DeleteField protected Map<BlockPos, BlockEntity> blockEntities;
    @Mutable @Shadow @Final @RestoreFinal protected ChunkPos chunkPos;
    @Mutable @Shadow @Final @RestoreFinal protected Map<Heightmap.Types, Heightmap> heightmaps;
    @Mutable @Shadow @Final @RestoreFinal protected LevelHeightAccessor levelHeightAccessor;
    @Shadow @Final @DeleteField protected Map<BlockPos, CompoundTag> pendingBlockEntities;
    @Mutable @Shadow @Final @RestoreFinal protected ShortList[] postProcessing;
    @Mutable @Shadow @Final @RestoreFinal protected LevelChunkSection[] sections;
    @Mutable @Shadow @Final @RestoreFinal protected UpgradeData upgradeData;
    //TODO very inefficient
    @Unique private volatile boolean @Nullable [] blockEmptinessMap;
    @Unique private volatile SWMRShortArray[] blockShorts;
    @Shadow private long inhabitedTime;
    //TODO very inefficient
    @Unique private volatile boolean @Nullable [] skyEmptinessMap;
    @Unique private volatile SWMRNibbleArray[] skyNibbles;
    @Mutable @Shadow @Final @RestoreFinal private Map<ConfiguredStructureFeature<?, ?>, StructureStart> structureStarts;
    @Mutable @Shadow @Final @RestoreFinal private Map<ConfiguredStructureFeature<?, ?>, LongSet> structuresRefences;

    @ModifyConstructor
    public Mixin_CF_ChunkAccess(ChunkPos chunkPos,
                                UpgradeData upgradeData,
                                LevelHeightAccessor levelHeightAccessor,
                                Registry<Biome> registry,
                                long inhabitedTime,
                                LevelChunkSection @Nullable [] levelChunkSections,
                                @Nullable BlendingData blendingData) {
        this.heightmaps = Maps.newEnumMap(Heightmap.Types.class);
        this.structureStarts = new O2OHashMap<>();
        this.structuresRefences = new O2OHashMap<>();
        this.chunkPos = chunkPos;
        this.upgradeData = upgradeData;
        this.levelHeightAccessor = levelHeightAccessor;
        this.sections = new LevelChunkSection[levelHeightAccessor.getSectionsCount()];
        this.inhabitedTime = inhabitedTime;
        this.postProcessing = new ShortList[levelHeightAccessor.getSectionsCount()];
        this.blendingData = blendingData;
        if (levelChunkSections != null) {
            if (this.sections.length == levelChunkSections.length) {
                System.arraycopy(levelChunkSections, 0, this.sections, 0, this.sections.length);
            }
            else {
                LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", levelChunkSections.length, this.sections.length);
            }
        }
        replaceMissingSections(levelHeightAccessor, registry, this.sections);
        this.blockEntities_ = new L2OHashMap<>();
        this.pendingBlockEntities_ = new L2OHashMap<>();
    }

    @Shadow
    private static void replaceMissingSections(LevelHeightAccessor levelHeightAccessor,
                                               Registry<Biome> registry, LevelChunkSection[] levelChunkSections) {
        throw new AbstractMethodError();
    }

    @Override
    public L2OMap<BlockEntity> blockEntities_() {
        return this.blockEntities_;
    }

    @Override
    public boolean @Nullable [] getBlockEmptinessMap() {
        return this.blockEmptinessMap;
    }

    /**
     * @author TheGreatWolf
     * @reason Do not use, iterate over the maps instead
     */
    @Overwrite
    public Set<BlockPos> getBlockEntitiesPos() {
        Evolution.warn("getBlockEntitiesPos() should not be called!");
        OSet<BlockPos> set = new OHashSet<>(this.pendingBlockEntities_.size() + this.blockEntities_.size());
        LongIterator it = this.pendingBlockEntities_.keySet().longIterator();
        while (it.hasNext()) {
            //noinspection ObjectAllocationInLoop
            set.add(BlockPos.of(it.nextLong()));
        }
        it = this.blockEntities_.keySet().longIterator();
        while (it.hasNext()) {
            //noinspection ObjectAllocationInLoop
            set.add(BlockPos.of(it.nextLong()));
        }
        return set;
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    public @Nullable CompoundTag getBlockEntityNbt(BlockPos pos) {
        Evolution.warn("getBlockEntityNbt(BlockPos) should not be called!");
        return this.getBlockEntityNbt_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public @Nullable CompoundTag getBlockEntityNbt_(int x, int y, int z) {
        return this.pendingBlockEntities_.get(BlockPos.asLong(x, y, z));
    }

    @Override
    public SWMRShortArray[] getBlockShorts() {
        return this.blockShorts;
    }

    /**
     * @author TheGreatWolf
     * @reason Remove computeIfAbsent
     */
    @Overwrite
    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types type) {
        Heightmap heightmap = this.heightmaps.get(type);
        if (heightmap == null) {
            heightmap = new Heightmap((ChunkAccess) (Object) this, type);
            this.heightmaps.put(type, heightmap);
        }
        return heightmap;
    }

    @Override
    public boolean @Nullable [] getSkyEmptinessMap() {
        return this.skyEmptinessMap;
    }

    @Override
    public SWMRNibbleArray[] getSkyNibbles() {
        return this.skyNibbles;
    }

    @Override
    public Map<Heightmap.Types, Heightmap> heightmaps_() {
        return this.heightmaps;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void markPosForPostprocessing(BlockPos pos) {
        Evolution.warn("markPosForPostprocessing(BlockPos) should not be called!");
        this.markPosForPostprocessing_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void markPosForPostprocessing_(int x, int y, int z) {
        LOGGER.warn("Trying to mark a block for PostProcessing at [{}, {}, {}], but this operation is not supported.", x, y, z);
    }

    @Override
    public L2OMap<CompoundTag> pendingBlockEntities_() {
        return this.pendingBlockEntities_;
    }

    @Override
    public void setBlockEmptinessMap(final boolean @Nullable [] emptinessMap) {
        this.blockEmptinessMap = emptinessMap;
    }

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Overwrite
    public void setBlockEntityNbt(CompoundTag tag) {
        this.pendingBlockEntities_.put(TEUtils.getPosFromTag(tag), tag);
    }

    @Override
    public void setBlockShorts(SWMRShortArray[] shorts) {
        this.blockShorts = shorts;
    }

    @Override
    public void setSkyEmptinessMap(boolean @Nullable [] emptinessMap) {
        this.skyEmptinessMap = emptinessMap;
    }

    @Override
    public void setSkyNibbles(SWMRNibbleArray[] nibbles) {
        this.skyNibbles = nibbles;
    }
}
