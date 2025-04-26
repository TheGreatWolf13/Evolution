package tgw.evolution.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.sets.OLinkedHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mixin(PrimaryLevelData.class)
public abstract class Mixin_CFM_PrimaryLevelData implements ServerLevelData, WorldData {

    @Shadow @Final private static Logger LOGGER;
    @Shadow private int clearWeatherTime;
    @Shadow private @Nullable CompoundTag customBossEvents;
    @Shadow private long dayTime;
    @Shadow private boolean difficultyLocked;
    @Shadow private CompoundTag endDragonFightData;
    @Mutable @Shadow @Final @RestoreFinal private @Nullable DataFixer fixerUpper;
    @Shadow private long gameTime;
    @Shadow private boolean initialized;
    @Shadow @Final @DeleteField private Set<String> knownServerBrands;
    @Unique private final OSet<String> knownServerBrands_;
    @Shadow private @Nullable CompoundTag loadedPlayerTag;
    @Mutable @Shadow @Final @RestoreFinal private int playerDataVersion;
    @Shadow private int rainTime;
    @Shadow private boolean raining;
    @Mutable @Shadow @Final @RestoreFinal private TimerQueue<MinecraftServer> scheduledEvents;
    @Shadow private LevelSettings settings;
    @Shadow private float spawnAngle;
    @Shadow private int thunderTime;
    @Shadow private boolean thundering;
    @Mutable @Shadow @Final @RestoreFinal private int version;
    @Shadow private @Nullable UUID wanderingTraderId;
    @Shadow private int wanderingTraderSpawnChance;
    @Shadow private int wanderingTraderSpawnDelay;
    @Shadow private boolean wasModded;
    @Shadow private WorldBorder.Settings worldBorder;
    @Mutable @Shadow @Final @RestoreFinal private WorldGenSettings worldGenSettings;
    @Mutable @Shadow @Final @RestoreFinal private Lifecycle worldGenSettingsLifecycle;
    @Shadow private int xSpawn;
    @Shadow private int ySpawn;
    @Shadow private int zSpawn;

    @ModifyConstructor
    public Mixin_CFM_PrimaryLevelData(@Nullable DataFixer dataFixer, int playerDataVersion, @Nullable CompoundTag compoundTag, boolean bl, int j, int k, int l, float f, long m, long n, int o, int p, int q, boolean bl2, int r, boolean bl3, boolean bl4, boolean bl5, WorldBorder.Settings settings, int s, int t, @Nullable UUID uUID, Set<String> set, TimerQueue<MinecraftServer> timerQueue, @Nullable CompoundTag compoundTag2, CompoundTag compoundTag3, LevelSettings levelSettings, WorldGenSettings worldGenSettings, Lifecycle lifecycle) {
        this.fixerUpper = dataFixer;
        this.wasModded = bl;
        this.xSpawn = j;
        this.ySpawn = k;
        this.zSpawn = l;
        this.spawnAngle = f;
        this.gameTime = m;
        this.dayTime = n;
        this.version = o;
        this.clearWeatherTime = p;
        this.rainTime = q;
        this.raining = bl2;
        this.thunderTime = r;
        this.thundering = bl3;
        this.initialized = bl4;
        this.difficultyLocked = bl5;
        this.worldBorder = settings;
        this.wanderingTraderSpawnDelay = s;
        this.wanderingTraderSpawnChance = t;
        this.wanderingTraderId = uUID;
        this.knownServerBrands_ = (OSet<String>) set;
        this.loadedPlayerTag = compoundTag;
        this.playerDataVersion = playerDataVersion;
        this.scheduledEvents = timerQueue;
        this.customBossEvents = compoundTag2;
        this.endDragonFightData = compoundTag3;
        this.settings = levelSettings;
        this.worldGenSettings = worldGenSettings;
        this.worldGenSettingsLifecycle = lifecycle;
    }

    @ModifyConstructor
    public Mixin_CFM_PrimaryLevelData(LevelSettings levelSettings, WorldGenSettings worldGenSettings, Lifecycle lifecycle) {
        this(null, SharedConstants.getCurrentVersion().getWorldVersion(), null, false, 0, 0, 0, 0.0F, 0L, 0L, 19_133, 0, 0, false, 0, false, false, false, WorldBorder.DEFAULT_SETTINGS, 0, 0, null, new OLinkedHashSet<>(), new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS), null, new CompoundTag(), levelSettings.copy(), worldGenSettings, lifecycle);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static void method_29030(CompoundTag par1, Tag par2) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static void method_29587(CompoundTag par1, Tag par2) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @Override
    public Set<String> getKnownServerBrands() {
        return this.knownServerBrands_.view();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @Override
    public void setModdedInfo(String brand, boolean modded) {
        this.knownServerBrands_.add(brand);
        this.wasModded |= modded;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void setTagData(RegistryAccess registryAccess, CompoundTag tag, @Nullable CompoundTag playerTag) {
        ListTag serverBrands = new ListTag();
        OSet<String> knownServerBrands = this.knownServerBrands_;
        for (long it = knownServerBrands.beginIteration(); knownServerBrands.hasNextIteration(it); it = knownServerBrands.nextEntry(it)) {
            serverBrands.add(StringTag.valueOf(knownServerBrands.getIteration(it)));
        }
        tag.put("ServerBrands", serverBrands);
        tag.putBoolean("WasModded", this.wasModded);
        CompoundTag versionTag = new CompoundTag();
        versionTag.putString("Name", SharedConstants.getCurrentVersion().getName());
        versionTag.putInt("Id", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        versionTag.putBoolean("Snapshot", !SharedConstants.getCurrentVersion().isStable());
        versionTag.putString("Series", SharedConstants.getCurrentVersion().getDataVersion().getSeries());
        tag.put("Version", versionTag);
        tag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        RegistryOps<Tag> dynamicOps = RegistryOps.create(NbtOps.INSTANCE, registryAccess);
        Optional<Tag> result = WorldGenSettings.CODEC.encodeStart(dynamicOps, this.worldGenSettings).resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error));
        if (result.isPresent()) {
            tag.put("WorldGenSettings", result.get());
        }
        tag.putInt("GameType", this.settings.gameType().getId());
        tag.putInt("SpawnX", this.xSpawn);
        tag.putInt("SpawnY", this.ySpawn);
        tag.putInt("SpawnZ", this.zSpawn);
        tag.putFloat("SpawnAngle", this.spawnAngle);
        tag.putLong("Time", this.gameTime);
        tag.putLong("DayTime", this.dayTime);
        tag.putLong("LastPlayed", Util.getEpochMillis());
        tag.putString("LevelName", this.settings.levelName());
        tag.putInt("version", 19_133);
        tag.putInt("clearWeatherTime", this.clearWeatherTime);
        tag.putInt("rainTime", this.rainTime);
        tag.putBoolean("raining", this.raining);
        tag.putInt("thunderTime", this.thunderTime);
        tag.putBoolean("thundering", this.thundering);
        tag.putBoolean("hardcore", this.settings.hardcore());
        tag.putBoolean("allowCommands", this.settings.allowCommands());
        tag.putBoolean("initialized", this.initialized);
        this.worldBorder.write(tag);
        tag.putByte("Difficulty", (byte) this.settings.difficulty().getId());
        tag.putBoolean("DifficultyLocked", this.difficultyLocked);
        tag.put("GameRules", this.settings.gameRules().createTag());
        tag.put("DragonFight", this.endDragonFightData);
        if (playerTag != null) {
            tag.put("Player", playerTag);
        }
        Optional<Tag> datapacks = DataPackConfig.CODEC.encodeStart(NbtOps.INSTANCE, this.settings.getDataPackConfig()).result();
        if (datapacks.isPresent()) {
            tag.put("DataPacks", datapacks.get());
        }
        if (this.customBossEvents != null) {
            tag.put("CustomBossEvents", this.customBossEvents);
        }
        tag.put("ScheduledEvents", this.scheduledEvents.store());
        tag.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
        tag.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);
        if (this.wanderingTraderId != null) {
            tag.putUUID("WanderingTraderId", this.wanderingTraderId);
        }
    }
}
