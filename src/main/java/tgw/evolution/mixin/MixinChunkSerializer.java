package tgw.evolution.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.blocks.tileentities.TEUtils;
import tgw.evolution.capabilities.chunk.AtmStorage;
import tgw.evolution.capabilities.chunk.IntegrityStorage;
import tgw.evolution.capabilities.chunk.StabilityStorage;
import tgw.evolution.hooks.ChunkHooks;
import tgw.evolution.patches.PatchLevelChunk;
import tgw.evolution.util.NBTHelper;
import tgw.evolution.util.collection.ArrayHelper;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.collection.sets.RSet;
import tgw.evolution.util.collection.sets.SimpleEnumSet;
import tgw.evolution.world.lighting.SWMRNibbleArray;
import tgw.evolution.world.lighting.SWMRShortArray;
import tgw.evolution.world.lighting.StarLightEngine;

import java.util.Map;

@Mixin(ChunkSerializer.class)
public abstract class MixinChunkSerializer {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private static Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC;

    @Shadow
    public static ChunkStatus.ChunkType getChunkTypeFromTag(@Nullable CompoundTag pChunkNBT) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static ListTag packOffsets(ShortList[] pList) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason Add AtmStorage to ChunkSections.
     */
    @Overwrite
    public static ProtoChunk read(ServerLevel level, PoiManager poiManager, ChunkPos pos, CompoundTag tag) {
        int xPos = tag.getInt("xPos");
        int zPos = tag.getInt("zPos");
        if (pos.x != xPos || pos.z != zPos) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got [{}, {}])", pos, pos, xPos, zPos);
        }
        UpgradeData upgradeData = tag.contains("UpgradeData", Tag.TAG_COMPOUND) ? new UpgradeData(tag.getCompound("UpgradeData"), level) : UpgradeData.EMPTY;
        boolean isLightOn = tag.getBoolean("isLightOn");
        ListTag sections = tag.getList("sections", Tag.TAG_COMPOUND);
        int sectionsCount = level.getSectionsCount();
        LevelChunkSection[] levelChunkSections = new LevelChunkSection[sectionsCount];
        boolean hasSkyLight = level.dimensionType().hasSkyLight();
        ChunkSource chunkSource = level.getChunkSource();
        LevelLightEngine lightEngine = chunkSource.getLightEngine();
        Registry<Biome> registry = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        Codec<PalettedContainer<Holder<Biome>>> codec = makeBiomeCodec(registry);
        final int minLightSection = lightEngine.getMinLightSection();
        SWMRShortArray[] blockNibbles = StarLightEngine.getFilledEmptyLightShort(level);
        SWMRNibbleArray[] skyNibbles = StarLightEngine.getFilledEmptyLightNibble(level);
        for (int i = 0; i < sections.size(); i++) {
            CompoundTag compound = sections.getCompound(i);
            int y = compound.getByte("Y");
            int index = level.getSectionIndexFromSectionY(y);
            if (index >= 0 && index < levelChunkSections.length) {
                PalettedContainer<BlockState> stateContainer;
                if (compound.contains("block_states", Tag.TAG_COMPOUND)) {
                    //noinspection ObjectAllocationInLoop
                    stateContainer = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, compound.getCompound("block_states"))
                                                      .promotePartial(s -> logErrors(pos, y, s))
                                                      .getOrThrow(false, LOGGER::error);
                }
                else {
                    //noinspection ObjectAllocationInLoop
                    stateContainer = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(),
                                                             PalettedContainer.Strategy.SECTION_STATES);
                }
                PalettedContainer<Holder<Biome>> biomeContainer;
                if (compound.contains("biomes", Tag.TAG_COMPOUND)) {
                    //noinspection ObjectAllocationInLoop
                    biomeContainer = codec.parse(NbtOps.INSTANCE, compound.getCompound("biomes"))
                                          .promotePartial(s -> logErrors(pos, y, s))
                                          .getOrThrow(false, LOGGER::error);
                }
                else {
                    //noinspection ObjectAllocationInLoop
                    biomeContainer = new PalettedContainer<>(registry.asHolderIdMap(), registry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
                }
                //noinspection ObjectAllocationInLoop
                AtmStorage atm = AtmStorage.read(NBTHelper.getCompound(compound, "atm"));
                //noinspection ObjectAllocationInLoop
                IntegrityStorage integrity = IntegrityStorage.read(NBTHelper.getCompound(compound, "integrity"));
                //noinspection ObjectAllocationInLoop
                IntegrityStorage loadFactor = IntegrityStorage.read(NBTHelper.getCompound(compound, "loadFactor"));
                //noinspection ObjectAllocationInLoop
                StabilityStorage stability = StabilityStorage.read(NBTHelper.getCompound(compound, "stability"));
                //noinspection ObjectAllocationInLoop
                LevelChunkSection section = new LevelChunkSection(y, stateContainer, biomeContainer);
                section.setAtmStorage(atm);
                section.setIntegrityStorage(integrity);
                section.setLoadFactorStorage(loadFactor);
                section.setStabilityStorage(stability);
                levelChunkSections[index] = section;
                poiManager.checkConsistencyWithBlocks(pos, section);
            }
            if (isLightOn) {
                if (compound.contains("BlockLight", Tag.TAG_BYTE_ARRAY)) {
                    //noinspection ObjectAllocationInLoop
                    blockNibbles[y - minLightSection] = new SWMRShortArray(compound.getByteArray("BlockLight"), compound.getInt("BlockLightState"));
                }
                else {
                    //noinspection ObjectAllocationInLoop
                    blockNibbles[y - minLightSection] = new SWMRShortArray(null, compound.getInt("BlockLightState"));
                }
                if (hasSkyLight) {
                    if (compound.contains("SkyLight", Tag.TAG_BYTE_ARRAY)) {
                        //noinspection ObjectAllocationInLoop
                        skyNibbles[y - minLightSection] = new SWMRNibbleArray(compound.getByteArray("SkyLight"), compound.getInt("SkyLightState"));
                    }
                    else {
                        //noinspection ObjectAllocationInLoop
                        skyNibbles[y - minLightSection] = new SWMRNibbleArray(null, compound.getInt("SkyLightState"));
                    }
                }
            }
        }
        long inhabitedTime = tag.getLong("InhabitedTime");
        ChunkStatus.ChunkType chunkType = getChunkTypeFromTag(tag);
        BlendingData blendingData;
        if (tag.contains("blending_data", Tag.TAG_COMPOUND)) {
            blendingData = BlendingData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, tag.getCompound("blending_data")))
                                             .resultOrPartial(LOGGER::error)
                                             .orElse(null);
        }
        else {
            blendingData = null;
        }
        ChunkAccess chunkAccess;
        if (chunkType == ChunkStatus.ChunkType.LEVELCHUNK) {
            LevelChunkTicks<Block> blockTicks = LevelChunkTicks.load(tag.getList("block_ticks", Tag.TAG_COMPOUND), s -> Registry.BLOCK.getOptional(ResourceLocation.tryParse(s)), pos);
            LevelChunkTicks<Fluid> fluidTicks = LevelChunkTicks.load(tag.getList("fluid_ticks", Tag.TAG_COMPOUND), s -> Registry.FLUID.getOptional(ResourceLocation.tryParse(s)), pos);
            LevelChunk chunk = new LevelChunk(level.getLevel(), pos, upgradeData, blockTicks, fluidTicks, inhabitedTime, levelChunkSections, postLoadChunk(level, tag), blendingData);
            if (tag.contains("Storage")) {
                chunk.getChunkStorage().deserializeNBT(tag.getCompound("Storage"));
            }
            chunkAccess = chunk;
        }
        else {
            ProtoChunkTicks<Block> blockTicks = ProtoChunkTicks.load(tag.getList("block_ticks", Tag.TAG_COMPOUND), s -> Registry.BLOCK.getOptional(ResourceLocation.tryParse(s)), pos);
            ProtoChunkTicks<Fluid> fluidTicks = ProtoChunkTicks.load(tag.getList("fluid_ticks", Tag.TAG_COMPOUND), s -> Registry.FLUID.getOptional(ResourceLocation.tryParse(s)), pos);
            ProtoChunk protoChunk = new ProtoChunk(pos, upgradeData, levelChunkSections, blockTicks, fluidTicks, level, registry, blendingData);
            chunkAccess = protoChunk;
            protoChunk.setInhabitedTime(inhabitedTime);
            ChunkStatus chunkStatus = ChunkStatus.byName(tag.getString("Status"));
            protoChunk.setStatus(chunkStatus);
            if (chunkStatus.isOrAfter(ChunkStatus.FEATURES)) {
                protoChunk.setLightEngine(lightEngine);
            }
            boolean lightDone = chunkStatus.isOrAfter(ChunkStatus.LIGHT);
            if (!isLightOn && lightDone) {
                for (BlockPos p : BlockPos.betweenClosed(pos.getMinBlockX(), level.getMinBuildHeight(), pos.getMinBlockZ(), pos.getMaxBlockX(), level.getMaxBuildHeight() - 1, pos.getMaxBlockZ())) {
                    if (chunkAccess.getBlockState(p).getLightEmission() != 0) {
                        protoChunk.addLight(p);
                    }
                }
            }
        }
        chunkAccess.setLightCorrect(isLightOn);
        CompoundTag heightmaps = tag.getCompound("Heightmaps");
        RSet<Heightmap.Types> types = new SimpleEnumSet<>(Heightmap.Types.class);
        for (Heightmap.Types type : chunkAccess.getStatus().heightmapsAfter()) {
            String s = type.getSerializationKey();
            if (heightmaps.contains(s, Tag.TAG_LONG_ARRAY)) {
                chunkAccess.setHeightmap(type, heightmaps.getLongArray(s));
            }
            else {
                types.add(type);
            }
        }
        Heightmap.primeHeightmaps(chunkAccess, types);
        CompoundTag structures = tag.getCompound("structures");
        chunkAccess.setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel(level), structures, level.getSeed()));
        ChunkHooks.fixNullStructureReferences(chunkAccess, unpackStructureReferences(level.registryAccess(), pos, structures));
        if (tag.getBoolean("shouldSave")) {
            chunkAccess.setUnsaved(true);
        }
        ListTag postProcessing = tag.getList("PostProcessing", Tag.TAG_LIST);
        for (int i = 0; i < postProcessing.size(); ++i) {
            ListTag list = postProcessing.getList(i);
            for (int j = 0; j < list.size(); ++j) {
                chunkAccess.addPackedPostProcess(list.getShort(j), i);
            }
        }
        if (chunkType == ChunkStatus.ChunkType.LEVELCHUNK) {
            chunkAccess.setBlockShorts(blockNibbles);
            chunkAccess.setSkyNibbles(skyNibbles);
            //Load event
            return new ImposterProtoChunk((LevelChunk) chunkAccess, false);
        }
        ProtoChunk protoChunk = (ProtoChunk) chunkAccess;
        ListTag entities = tag.getList("entities", Tag.TAG_COMPOUND);
        for (int i = 0; i < entities.size(); ++i) {
            protoChunk.addEntity(entities.getCompound(i));
        }
        ListTag blockEntities = tag.getList("block_entities", Tag.TAG_COMPOUND);
        for (int i = 0; i < blockEntities.size(); ++i) {
            CompoundTag compound = blockEntities.getCompound(i);
            chunkAccess.setBlockEntityNbt(compound);
        }
        ListTag lights = tag.getList("Lights", Tag.TAG_LIST);
        for (int i = 0; i < lights.size(); ++i) {
            ListTag list = lights.getList(i);
            for (int j = 0; j < list.size(); ++j) {
                protoChunk.addLight(list.getShort(j), i);
            }
        }
        CompoundTag carvingMasks = tag.getCompound("CarvingMasks");
        for (String s : carvingMasks.getAllKeys()) {
            GenerationStep.Carving carving = GenerationStep.Carving.valueOf(s);
            //noinspection ObjectAllocationInLoop
            protoChunk.setCarvingMask(carving, new CarvingMask(carvingMasks.getLongArray(s), chunkAccess.getMinBuildHeight()));
        }
        protoChunk.setBlockShorts(blockNibbles);
        protoChunk.setSkyNibbles(skyNibbles);
        //Load event
        return protoChunk;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static CompoundTag write(ServerLevel level, ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        CompoundTag tag = new CompoundTag();
        tag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        tag.putInt("xPos", pos.x);
        tag.putInt("yPos", chunk.getMinSection());
        tag.putInt("zPos", pos.z);
        tag.putLong("LastUpdate", level.getGameTime());
        tag.putLong("InhabitedTime", chunk.getInhabitedTime());
        tag.putString("Status", chunk.getStatus().getName());
        BlendingData blendingData = chunk.getBlendingData();
        if (blendingData != null) {
            BlendingData.CODEC.encodeStart(NbtOps.INSTANCE, blendingData).resultOrPartial(LOGGER::error).ifPresent(s -> tag.put("blending_data", s));
        }
        LevelChunkSection[] sections = chunk.getSections();
        ListTag sectionsList = new ListTag();
        LevelLightEngine lightEngine = level.getChunkSource().getLightEngine();
        Registry<Biome> registry = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        Codec<PalettedContainer<Holder<Biome>>> codec = makeBiomeCodec(registry);
        boolean lightCorrect = chunk.isLightCorrect();
        SWMRShortArray[] blockNibbles = chunk.getBlockShorts();
        SWMRNibbleArray[] skyNibbles = chunk.getSkyNibbles();
        int minLightSection = lightEngine.getMinLightSection();
        for (int i = minLightSection; i < lightEngine.getMaxLightSection(); ++i) {
            int index = chunk.getSectionIndexFromSectionY(i);
            boolean validIndex = index >= 0 && index < sections.length;
            SWMRShortArray.SaveState blockNibble = blockNibbles[i - minLightSection].getSaveState();
            SWMRNibbleArray.SaveState skyNibble = skyNibbles[i - minLightSection].getSaveState();
            if (validIndex || blockNibble != null || skyNibble != null) {
                //noinspection ObjectAllocationInLoop
                CompoundTag sectionTag = new CompoundTag();
                if (validIndex) {
                    LevelChunkSection section = sections[index];
                    //noinspection ObjectAllocationInLoop
                    sectionTag.put("block_states", BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, section.getStates()).getOrThrow(false, LOGGER::error));
                    //noinspection ObjectAllocationInLoop
                    sectionTag.put("biomes", codec.encodeStart(NbtOps.INSTANCE, section.getBiomes()).getOrThrow(false, LOGGER::error));
                    //noinspection ObjectAllocationInLoop
                    sectionTag.put("atm", section.getAtmStorage().serialize());
                    //noinspection ObjectAllocationInLoop
                    sectionTag.put("integrity", section.getIntegrityStorage().serialize());
                    //noinspection ObjectAllocationInLoop
                    sectionTag.put("loadFactor", section.getLoadFactorStorage().serialize());
                    //noinspection ObjectAllocationInLoop
                    sectionTag.put("stability", section.getStabilityStorage().serialize());
                }
                if (blockNibble != null) {
                    if (blockNibble.data != null) {
                        sectionTag.putByteArray("BlockLight", blockNibble.data);
                    }
                    sectionTag.putInt("BlockLightState", blockNibble.state);
                }
                if (skyNibble != null) {
                    if (skyNibble.data != null) {
                        sectionTag.putByteArray("SkyLight", skyNibble.data);
                    }
                    sectionTag.putInt("SkyLightState", skyNibble.state);
                }
                if (!sectionTag.isEmpty()) {
                    sectionTag.putByte("Y", (byte) i);
                    sectionsList.add(sectionTag);
                }
            }
        }
        tag.put("sections", sectionsList);
        if (lightCorrect) {
            tag.putBoolean("isLightOn", true);
        }
        ListTag blockEntities = new ListTag();
        L2OMap<CompoundTag> pendingBlockEntities = chunk.pendingBlockEntities_();
        for (L2OMap.Entry<CompoundTag> e = pendingBlockEntities.fastEntries(); e != null; e = pendingBlockEntities.fastEntries()) {
            long p = e.key();
            CompoundTag teTag = chunk.getBlockEntityNbtForSaving_(BlockPos.getX(p), BlockPos.getY(p), BlockPos.getZ(p));
            if (teTag != null) {
                blockEntities.add(teTag);
            }
        }
        L2OMap<BlockEntity> blockEntitiess = chunk.blockEntities_();
        for (L2OMap.Entry<BlockEntity> e = blockEntitiess.fastEntries(); e != null; e = blockEntitiess.fastEntries()) {
            long p = e.key();
            CompoundTag teTag = chunk.getBlockEntityNbtForSaving_(BlockPos.getX(p), BlockPos.getY(p), BlockPos.getZ(p));
            if (teTag != null) {
                blockEntities.add(teTag);
            }
        }
        tag.put("block_entities", blockEntities);
        if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
            ProtoChunk protoChunk = (ProtoChunk) chunk;
            ListTag entitiesList = new ListTag();
            entitiesList.addAll(protoChunk.getEntities());
            tag.put("entities", entitiesList);
            tag.put("Lights", packOffsets(protoChunk.getPackedLights()));
            CompoundTag carvingMasks = new CompoundTag();
            for (GenerationStep.Carving carving : ArrayHelper.CARVINGS) {
                CarvingMask carvingmask = protoChunk.getCarvingMask(carving);
                if (carvingmask != null) {
                    carvingMasks.putLongArray(carving.toString(), carvingmask.toArray());
                }
            }
            tag.put("CarvingMasks", carvingMasks);
        }
        else {
            tag.put("Storage", ((PatchLevelChunk) chunk).getChunkStorage().serializeNBT());
        }
        saveTicks(level, tag, chunk.getTicksForSerialization());
        tag.put("PostProcessing", packOffsets(chunk.getPostProcessing()));
        CompoundTag heightmaps = new CompoundTag();
        for (Map.Entry<Heightmap.Types, Heightmap> entry : chunk.getHeightmaps()) {
            if (chunk.getStatus().heightmapsAfter().contains(entry.getKey())) {
                //noinspection ObjectAllocationInLoop
                heightmaps.put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
            }
        }
        tag.put("Heightmaps", heightmaps);
        tag.put("structures", packStructureData(StructurePieceSerializationContext.fromLevel(level), pos, chunk.getAllStarts(), chunk.getAllReferences()));
        return tag;
    }

    @Shadow
    private static @Nullable ListTag getListOfCompoundsOrNull(CompoundTag compoundTag, String string) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static void logErrors(ChunkPos pChunkPos, int pChunkSectionY, String pErrorMessage) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static Codec<PalettedContainer<Holder<Biome>>> makeBiomeCodec(Registry<Biome> pBiomeRegistry) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private static void method_39797(@Nullable ListTag entities, ServerLevel level, @Nullable ListTag blockEntities, LevelChunk chunk) {
        if (entities != null) {
            level.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(entities, level));
        }
        if (blockEntities != null) {
            for (int i = 0, len = blockEntities.size(); i < len; ++i) {
                CompoundTag nbt = blockEntities.getCompound(i);
                boolean keepPacked = nbt.getBoolean("keepPacked");
                if (keepPacked) {
                    chunk.setBlockEntityNbt(nbt);
                }
                else {
                    int x = nbt.getInt("x");
                    int y = nbt.getInt("y");
                    int z = nbt.getInt("z");
                    BlockEntity blockEntity = TEUtils.loadStatic(x, y, z, chunk.getBlockState_(x, y, z), nbt);
                    if (blockEntity != null) {
                        chunk.setBlockEntity(blockEntity);
                    }
                }
            }
        }
    }

    @Shadow
    private static CompoundTag packStructureData(StructurePieceSerializationContext pContext,
                                                 ChunkPos pPos,
                                                 Map<ConfiguredStructureFeature<?, ?>, StructureStart> pStructureMap,
                                                 Map<ConfiguredStructureFeature<?, ?>, LongSet> pReferenceMap) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @SuppressWarnings("ConstantConditions")
    @Overwrite
    private static @Nullable LevelChunk.PostLoadProcessor postLoadChunk(ServerLevel level, CompoundTag compoundTag) {
        ListTag entities = getListOfCompoundsOrNull(compoundTag, "entities");
        ListTag blockEntities = getListOfCompoundsOrNull(compoundTag, "block_entities");
        if (entities == null && blockEntities == null) {
            return null;
        }
        return chunk -> method_39797(entities, level, blockEntities, chunk);
    }

    @Shadow
    private static void saveTicks(ServerLevel pLevel, CompoundTag pTag, ChunkAccess.TicksToSave pTicksToSave) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static Map<ConfiguredStructureFeature<?, ?>, LongSet> unpackStructureReferences(RegistryAccess pReigstryAccess, ChunkPos pPos, CompoundTag pTag) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static Map<ConfiguredStructureFeature<?, ?>, StructureStart> unpackStructureStart(
            StructurePieceSerializationContext pContext, CompoundTag pTag, long pSeed) {
        throw new AbstractMethodError();
    }
}
