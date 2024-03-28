package tgw.evolution.util;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.storage.DataVersion;
import net.minecraft.world.level.storage.LevelVersion;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.collection.sets.OLinkedHashSet;
import tgw.evolution.util.collection.sets.OSet;
import tgw.evolution.util.constants.NBTType;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.BiConsumer;

import static net.minecraft.data.BuiltinRegistries.NOISE_GENERATOR_SETTINGS;

public final class NBTHelper {

    private static final CompoundTag EMPTY_COMPOUND = new CompoundTag() {
        @Override
        public @Nullable Tag put(String string, Tag tag) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putBoolean(String string, boolean bl) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putByte(String string, byte b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putByteArray(String string, byte[] bs) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putByteArray(String string, List<Byte> list) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putDouble(String string, double d) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putFloat(String string, float f) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putInt(String string, int i) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putIntArray(String string, int[] is) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putIntArray(String string, List<Integer> list) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putLong(String string, long l) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putLongArray(String string, long[] ls) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putLongArray(String string, List<Long> list) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putShort(String string, short s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putString(String string, String string2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putUUID(String string, UUID uUID) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(String string) {
            throw new UnsupportedOperationException();
        }
    };

    private NBTHelper() {
    }

    public static StringTag encode(ResourceLocation loc) {
        return StringTag.valueOf(loc.toString());
    }

    public static boolean getBooleanOrElse(CompoundTag tag, String key, boolean def) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_BYTE) {
            return ((NumericTag) t).getAsByte() == 1;
        }
        return def;
    }

    public static byte getByteOrElse(CompoundTag nbt, String key, byte orElse) {
        Tag t = nbt.get(key);
        if (t != null && t.getId() == Tag.TAG_BYTE) {
            return ((NumericTag) t).getAsByte();
        }
        return orElse;
    }

    public static @Nullable CompoundTag getCompound(CompoundTag tag, String key) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_COMPOUND) {
            return (CompoundTag) t;
        }
        return null;
    }

    /**
     * Use for reading only, do not write into this tag.
     */
    public static CompoundTag getCompoundOrEmpty(CompoundTag tag, String key) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_COMPOUND) {
            return (CompoundTag) t;
        }
        return EMPTY_COMPOUND;
    }

    public static CompoundTag getCompoundOrNew(CompoundTag tag, String key) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_COMPOUND) {
            return (CompoundTag) t;
        }
        return new CompoundTag();
    }

    /**
     * Can be safely used for writing. If the compound doesn't exist, it will be created and added into the original tag automatically.
     */
    public static CompoundTag getCompoundOrWrite(CompoundTag tag, String key) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_COMPOUND) {
            return (CompoundTag) t;
        }
        CompoundTag c = new CompoundTag();
        tag.put(key, c);
        return c;
    }

    public static double getDoubleClamped(CompoundTag tag, String key, double orElse, double min, double max) {
        return Mth.clamp(getDoubleOrElse(tag, key, orElse), min, max);
    }

    public static double getDoubleOrElse(CompoundTag tag, String key, double orElse) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_DOUBLE) {
            return ((NumericTag) t).getAsDouble();
        }
        return orElse;
    }

    public static float getFloatOrElse(CompoundTag tag, String key, float orElse) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_FLOAT) {
            return ((NumericTag) t).getAsFloat();
        }
        return orElse;
    }

    public static int getInt(CompoundTag nbt, String key) {
        Tag t = nbt.get(key);
        if (t != null && t.getId() == Tag.TAG_INT) {
            return ((NumericTag) t).getAsInt();
        }
        return 0;
    }

    public static int getIntClamped(CompoundTag tag, String key, int orElse, int min, int max) {
        return Mth.clamp(getIntOrElse(tag, key, orElse), min, max);
    }

    public static int getIntOrElse(CompoundTag tag, String key, int orElse) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_INT) {
            return ((NumericTag) t).getAsInt();
        }
        return orElse;
    }

    public static @Nullable ListTag getList(CompoundTag tag, String key) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_LIST) {
            return (ListTag) t;
        }
        return null;
    }

    public static @Nullable ListTag getListOf(CompoundTag tag, String key, @NBTType int type) {
        ListTag list = getList(tag, key);
        if (list == null || list.isEmpty() || list.getElementType() != type) {
            return null;
        }
        return list;
    }

    public static long getLongOrElse(CompoundTag tag, String key, long orElse) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_LONG) {
            return ((NumericTag) t).getAsLong();
        }
        return orElse;
    }

    public static OptionalLong getOptionalLong(CompoundTag tag, String key) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_LONG) {
            return OptionalLong.of(((NumericTag) t).getAsLong());
        }
        return OptionalLong.empty();
    }

    public static @Nullable String getString(CompoundTag tag, String key) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_STRING) {
            return t.getAsString();
        }
        return null;
    }

    public static OSet<String> getStringListAsLinkedSet(CompoundTag tag, String key) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_LIST) {
            ListTag list = (ListTag) t;
            if (list.getElementType() == Tag.TAG_STRING) {
                OSet<String> set = new OLinkedHashSet<>();
                for (int i = 0, len = list.size(); i < len; ++i) {
                    set.add(list.get(i).getAsString());
                }
                return set;
            }
        }
        return OSet.emptySet();
    }

    public static String getStringOrElse(CompoundTag tag, String key, String orElse) {
        Tag t = tag.get(key);
        if (t != null && t.getId() == Tag.TAG_STRING) {
            return t.getAsString();
        }
        return orElse;
    }

    public static @Nullable UUID getUUID(CompoundTag tag, String key) {
        Tag t = tag.get(key);
        if (t == null || t.getId() != Tag.TAG_INT_ARRAY) {
            return null;
        }
        int[] array = ((IntArrayTag) t).getAsIntArray();
        if (array.length != 4) {
            return null;
        }
        return SerializableUUID.uuidFromIntArray(array);
    }

    public static @Nullable ChunkGenerator parseChunkGenerator(CompoundTag tag, Logger logger) {
        String type = getString(tag, "type");
        if (type == null) {
            logger.error("Missing chunk generator!");
            return null;
        }
        return switch (type) {
            case "minecraft:noise" -> parseNoiseChunkGenerator(tag, logger);
            case "minecraft:flat" -> parseFlatChunkGenerator(tag, logger);
            case "minecraft:debug" -> parseDebugChunkGenerator();
            default -> {
                logger.error("Unknown chunk generator: {}", type);
                yield null;
            }
        };
    }

    public static LevelSettings parseLevelSettings(CompoundTag nbt, DataPackConfig dataPackConfig) {
        GameType gameType = GameType.byId(getIntOrElse(nbt, "GameType", 0));
        byte dif = getByteOrElse(nbt, "Difficulty", (byte) -1);
        Difficulty difficulty = dif == -1 ? Difficulty.NORMAL : Difficulty.byId(dif);
        GameRules gameRules = new GameRules();
        gameRules.loadFromTag(nbt.getCompound("GameRules"));
        return new LevelSettings(getStringOrElse(nbt, "LevelName", ""), gameType, getBooleanOrElse(nbt, "hardcore", false), difficulty, getBooleanOrElse(nbt, "allowCommands", gameType == GameType.CREATIVE), gameRules, dataPackConfig);
    }

    public static LevelVersion parseLevelVersion(CompoundTag nbt) {
        int i = nbt.getInt("version");
        long l = nbt.getLong("LastPlayed");
        CompoundTag version = getCompound(nbt, "Version");
        return version != null ? new LevelVersion(i, l, getStringOrElse(version, "Name", SharedConstants.getCurrentVersion().getName()), getIntOrElse(version, "Id", SharedConstants.getCurrentVersion().getDataVersion().getVersion()), getStringOrElse(version, "Series", DataVersion.MAIN_SERIES), getBooleanOrElse(version, "Snapshot", !SharedConstants.getCurrentVersion().isStable())) : new LevelVersion(i, l, "", 0, DataVersion.MAIN_SERIES, false);
    }

    public static PrimaryLevelData parsePrimaryLevelData(CompoundTag dataTag, DataFixer dataFixer, int version, @Nullable CompoundTag playerTag, LevelSettings levelSettings, LevelVersion levelVersion, WorldGenSettings worldGenSettings, Lifecycle lifecycle) {
        long time = getLongOrElse(dataTag, "Time", 0);
        TimerQueue timerQueue = new TimerQueue(TimerCallbacks.SERVER_CALLBACKS);
        ListTag scheduledEvents = getList(dataTag, "ScheduledEvents");
        if (scheduledEvents != null) {
            for (int i = 0, len = scheduledEvents.size(); i < len; ++i) {
                Tag t = scheduledEvents.get(i);
                if (t instanceof CompoundTag tag) {
                    timerQueue.loadEvent(tag);
                }
            }
        }
        return new PrimaryLevelData(dataFixer,
                                    version,
                                    playerTag,
                                    getBooleanOrElse(dataTag, "WasModded", false),
                                    getIntOrElse(dataTag, "SpawnX", 0),
                                    getIntOrElse(dataTag, "SpawnY", 0),
                                    getIntOrElse(dataTag, "SpawnZ", 0),
                                    getFloatOrElse(dataTag, "SpawnAngle", 0.0f),
                                    time,
                                    getLongOrElse(dataTag, "DayTime", time),
                                    levelVersion.levelDataVersion(),
                                    getIntOrElse(dataTag, "clearWeatherTime", 0),
                                    getIntOrElse(dataTag, "rainTime", 0),
                                    getBooleanOrElse(dataTag, "raining", false),
                                    getIntOrElse(dataTag, "thunderTime", 0),
                                    getBooleanOrElse(dataTag, "thundering", false),
                                    getBooleanOrElse(dataTag, "initialized", true),
                                    getBooleanOrElse(dataTag, "DifficultyLocked", false),
                                    parseWorldBorderSettings(dataTag, WorldBorder.DEFAULT_SETTINGS),
                                    getIntOrElse(dataTag, "WanderingTraderSpawnDelay", 0),
                                    getIntOrElse(dataTag, "WanderingTraderSpawnChance", 0),
                                    getUUID(dataTag, "WanderingTraderId"),
                                    getStringListAsLinkedSet(dataTag, "ServerBrand"),
                                    timerQueue,
                                    getCompoundOrNew(dataTag, "CustomBossEvents"),
                                    getCompoundOrNew(dataTag, "DragonFight"),
                                    levelSettings,
                                    worldGenSettings,
                                    lifecycle
        );
    }

    public static <T> @Nullable T parseRegistered(@Nullable Tag tag, Registry<T> registry, Logger logger) {
        ResourceLocation resLoc = parseResLoc(tag, logger);
        if (resLoc == null) {
            return null;
        }
        T t = registry.get(resLoc);
        if (t == null) {
            logger.error("Unknown {}: {}", registry.key().location(), resLoc);
            return null;
        }
        return t;
    }

    public static <T, O> void parseRegisteredTag(@Nullable Tag tag, Registry<T> registry, Logger logger, BiConsumer<T, O> consumer, BiConsumer<TagKey<T>, O> tagConsumer, O o) {
        String s = parseString(tag, logger);
        if (s == null) {
            return;
        }
        if (s.startsWith("#")) {
            ResourceLocation resLoc = parseResLocTag(s, logger);
            if (resLoc == null) {
                return;
            }
            TagKey<T> key = TagKey.create(registry.key(), resLoc);
            tagConsumer.accept(key, o);
            return;
        }
        ResourceLocation resLoc = parseResLoc(s, logger);
        T t = registry.get(resLoc);
        consumer.accept(t, o);
    }

    public static @Nullable ResourceLocation parseResLoc(@Nullable Tag tag, Logger logger) {
        String s = parseString(tag, logger);
        if (s == null) {
            return null;
        }
        return parseResLoc(s, logger);
    }

    public static @Nullable ResourceLocation parseResLoc(String name, Logger logger) {
        ResourceLocation location = ResourceLocation.tryParse(name);
        if (location == null) {
            logger.error("Not a valid resource location: {}", name);
            return null;
        }
        return location;
    }

    public static @Nullable ResourceLocation parseResLocTag(String name, Logger logger) {
        return parseResLoc(name.substring(1), logger);
    }

    public static <T> @Nullable ResourceKey<T> parseResourceKey(ResourceKey<Registry<T>> registry, @Nullable Tag tag, Logger logger) {
        ResourceLocation r = parseResLoc(tag, logger);
        if (r == null) {
            return null;
        }
        return ResourceKey.elementKey(registry).apply(r);
    }

    public static <T> ResourceKey<T> parseResourceKeyOrElse(ResourceKey<Registry<T>> registry, @Nullable Tag tag, Logger logger, ResourceKey<T> orElse) {
        ResourceKey<T> r = parseResourceKey(registry, tag, logger);
        if (r == null) {
            return orElse;
        }
        return r;
    }

    public static @Nullable String parseString(@Nullable Tag tag, Logger logger) {
        if (tag == null) {
            return null;
        }
        if (tag.getId() != Tag.TAG_STRING) {
            logger.error("Tag {} is not a string!", tag);
            return null;
        }
        return tag.getAsString();
    }

    public static WorldBorder.Settings parseWorldBorderSettings(CompoundTag tag, WorldBorder.Settings settings) {
        return new WorldBorder.Settings(Mth.clamp(getDoubleOrElse(tag, "BorderCenterX", settings.getCenterX()), -2.999_998_4E7, 2.999_998_4E7),
                                        Mth.clamp(getDoubleOrElse(tag, "BorderCenterZ", settings.getCenterZ()), -2.999_998_4E7, 2.999_998_4E7),
                                        getDoubleOrElse(tag, "BorderDamagePerBlock", settings.getDamagePerBlock()),
                                        getDoubleOrElse(tag, "BorderSafeZone", settings.getSafeZone()),
                                        getIntOrElse(tag, "BorderWarningBlocks", settings.getWarningBlocks()),
                                        getIntOrElse(tag, "BorderWarningTime", settings.getWarningTime()),
                                        getDoubleOrElse(tag, "BorderSize", settings.getSize()),
                                        getLongOrElse(tag, "BorderSizeLerpTime", settings.getSizeLerpTime()),
                                        getDoubleOrElse(tag, "BorderSizeLerpTarget", settings.getSizeLerpTarget())
        );
    }

    public static WorldGenSettings parseWorldGenSettings(CompoundTag tag, Logger logger) {
        CompoundTag worldGenSettings = getCompound(tag, "WorldGenSettings");
        if (worldGenSettings == null) {
            logger.error("Missing WorldGenSettings!");
            throw new RuntimeException("Missing WorldGenSettings!");
        }
        long seed = worldGenSettings.getLong("seed");
        boolean generateFeatures = getBooleanOrElse(worldGenSettings, "generate_features", true);
        boolean bonusChest = getBooleanOrElse(worldGenSettings, "bonus_chest", false);
        CompoundTag dimensions = getCompound(worldGenSettings, "dimensions");
        if (dimensions == null) {
            logger.error("Missing dimensions!");
            throw new RuntimeException("Missing dimensions!");
        }
        O2OMap<String, Tag> tags = dimensions.tags();
        WritableRegistry<LevelStem> writableRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), null);
        for (long it = tags.beginIteration(); tags.hasNextIteration(it); it = tags.nextEntry(it)) {
            Tag value = tags.getIterationValue(it);
            if (value instanceof CompoundTag compound) {
                String type = getString(compound, "type");
                if (type == null) {
                    continue;
                }
                DimensionType dimensionType;
                switch (type) {
                    case "minecraft:overworld" -> dimensionType = DimensionType.DEFAULT_OVERWORLD;
                    case "minecraft:the_nether" -> dimensionType = DimensionType.DEFAULT_NETHER;
                    case "minecraft:the_end" -> dimensionType = DimensionType.DEFAULT_END;
                    default -> {
                        continue;
                    }
                }
                CompoundTag generator = getCompound(compound, "generator");
                if (generator == null) {
                    continue;
                }
                ChunkGenerator chunkGenerator = parseChunkGenerator(generator, logger);
                if (chunkGenerator == null) {
                    continue;
                }
                //noinspection ObjectAllocationInLoop
                LevelStem stem = new LevelStem(Holder.direct(dimensionType), chunkGenerator);
                //noinspection ObjectAllocationInLoop
                writableRegistry.register(ResourceKey.elementKey(Registry.LEVEL_STEM_REGISTRY).apply(new ResourceLocation(tags.getIterationKey(it))), stem, Lifecycle.stable());
            }
        }
        return new WorldGenSettings(seed, generateFeatures, bonusChest, LevelStem.sortMap(writableRegistry));
    }

    public static NonNullList<ItemStack> readStackList(CompoundTag nbt) {
        NonNullList<ItemStack> list = NonNullList.withSize(getInt(nbt, "Size"), ItemStack.EMPTY);
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = getInt(itemTags, "Slot");
            if (slot >= 0 && slot < list.size()) {
                list.set(slot, ItemStack.of(itemTags));
            }
        }
        return list;
    }

    public static CompoundTag writeStackList(NonNullList<ItemStack> stacks) {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                //noinspection ObjectAllocationInLoop
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stacks.get(i).save(itemTag);
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    private static ChunkGenerator parseDebugChunkGenerator() {
        return new DebugLevelSource(BuiltinRegistries.STRUCTURE_SETS, BuiltinRegistries.BIOME);
    }

    private static @Nullable ChunkGenerator parseFlatChunkGenerator(CompoundTag tag, Logger logger) {
        CompoundTag settings = getCompound(tag, "settings");
        if (settings == null) {
            logger.error("Could not find settings!");
            return null;
        }
        Optional<HolderSet<StructureSet>> optional = Optional.empty();
        Tag structureOverrides = settings.get("structure_overrides");
        if (structureOverrides != null) {
            if (structureOverrides.getId() == Tag.TAG_STRING) {
                StructureSet structureSet = parseRegistered(structureOverrides, BuiltinRegistries.STRUCTURE_SETS, logger);
                if (structureSet != null) {
                    optional = Optional.of(HolderSet.direct(OList.of(Holder.direct(structureSet))));
                }
            }
            else if (structureOverrides.getId() == Tag.TAG_LIST) {
                ListTag list = (ListTag) structureOverrides;
                if (!list.isEmpty()) {
                    if (list.getElementType() != Tag.TAG_STRING) {
                        logger.error("Invalid structure_overrides!");
                    }
                    else {
                        OList<Holder<StructureSet>> holderList = new OArrayList<>();
                        for (int i = 0, len = list.size(); i < len; ++i) {
                            StructureSet structureSet = parseRegistered(list.get(i), BuiltinRegistries.STRUCTURE_SETS, logger);
                            if (structureSet == null) {
                                continue;
                            }
                            //noinspection ObjectAllocationInLoop
                            holderList.add(Holder.direct(structureSet));
                        }
                        optional = Optional.of(HolderSet.direct(holderList));
                    }
                }
            }
        }
        OList<FlatLayerInfo> layersList = new OArrayList<>();
        ListTag layers = getListOf(settings, "layers", Tag.TAG_COMPOUND);
        if (layers == null) {
            logger.error("Could not find layers!");
            return null;
        }
        for (int i = 0, len = layers.size(); i < len; ++i) {
            CompoundTag t = (CompoundTag) layers.get(i);
            int height = getIntClamped(t, "height", 0, 0, DimensionType.Y_SIZE);
            Block block = parseRegistered(t.get("block"), Registry.BLOCK, logger);
            if (block == null) {
                block = Blocks.AIR;
            }
            //noinspection ObjectAllocationInLoop
            layersList.add(new FlatLayerInfo(height, block));
        }
        boolean lakes = getBooleanOrElse(settings, "lakes", false);
        boolean features = getBooleanOrElse(settings, "features", false);
        Biome biome = parseRegistered(settings.get("biome"), BuiltinRegistries.BIOME, logger);
        Optional<Holder<Biome>> biomeHolder = biome != null ? Optional.of(Holder.direct(biome)) : Optional.empty();
        FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(BuiltinRegistries.BIOME, optional, layersList, lakes, features, biomeHolder);
        return new FlatLevelSource(BuiltinRegistries.STRUCTURE_SETS, flatLevelGeneratorSettings);
    }

    private static @Nullable ChunkGenerator parseNoiseChunkGenerator(CompoundTag tag, Logger logger) {
        CompoundTag biomeSourceTag = getCompound(tag, "biome_source");
        if (biomeSourceTag == null) {
            logger.error("Could not find biome source!");
            return null;
        }
        String type = getString(biomeSourceTag, "type");
        if (type == null) {
            logger.error("Could not find biome source type!");
            return null;
        }
        BiomeSource biomeSource = null;
        switch (type) {
            case "minecraft:fixed" -> {
                Biome biome = parseRegistered(biomeSourceTag.get("biome"), BuiltinRegistries.BIOME, logger);
                if (biome != null) {
                    biomeSource = new FixedBiomeSource(Holder.direct(biome));
                }
            }
            case "minecraft:multi_noise" -> {
                ResourceLocation resLoc = parseResLoc(biomeSourceTag.get("preset"), logger);
                if (resLoc != null) {
                    biomeSource = MultiNoiseBiomeSource.Preset.BY_NAME.get(resLoc).biomeSource(BuiltinRegistries.BIOME);
                }
            }
            case "minecraft:checkerboard" -> {
                Tag biomes = biomeSourceTag.get("biomes");
                if (biomes != null) {
                    OList<Holder<Biome>> holderList = new OArrayList<>();
                    if (biomes.getId() == Tag.TAG_STRING) {
                        parseRegisteredTag(biomes, BuiltinRegistries.BIOME, logger, (biome, list) -> {
                            if (biome != null) {
                                list.add(Holder.direct(biome));
                            }
                        }, (tagKey, list) -> list.addAll(BuiltinRegistries.BIOME.getTagOrEmpty(tagKey)), holderList);
                    }
                    else if (biomes.getId() == Tag.TAG_LIST) {
                        ListTag list = (ListTag) biomes;
                        if (!list.isEmpty() && list.getElementType() == Tag.TAG_STRING) {
                            for (int i = 0, len = list.size(); i < len; ++i) {
                                Biome biome = parseRegistered(list.get(i), BuiltinRegistries.BIOME, logger);
                                if (biome != null) {
                                    //noinspection ObjectAllocationInLoop
                                    holderList.add(Holder.direct(biome));
                                }
                            }
                        }
                    }
                    biomeSource = new CheckerboardColumnBiomeSource(HolderSet.direct(holderList), getIntClamped(biomeSourceTag, "scale", 2, 0, 62));
                }
            }
            case "minecraft:the_end" -> {
                biomeSource = new TheEndBiomeSource(BuiltinRegistries.BIOME, getLongOrElse(biomeSourceTag, "seed", 0));
            }
        }
        if (biomeSource == null) {
            logger.error("Could not parse biome source!");
            return null;
        }
        long seed = getLongOrElse(tag, "seed", 0);
        String settings = getString(tag, "settings");
        if (settings == null) {
            logger.error("Could not find settings!");
            return null;
        }
        NoiseGeneratorSettings noiseGeneratorSettings = NOISE_GENERATOR_SETTINGS.get(new ResourceLocation(settings));
        if (noiseGeneratorSettings == null) {
            logger.error("Could not find noise generator settings with id: {}", settings);
            return null;
        }
        return new NoiseBasedChunkGenerator(BuiltinRegistries.STRUCTURE_SETS, BuiltinRegistries.NOISE, biomeSource, seed, Holder.direct(noiseGeneratorSettings));
    }
}
