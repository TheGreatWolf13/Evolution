package tgw.evolution.stats;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.ServerStatisticsManager;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.io.FileUtils;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.network.PacketSCStatistics;
import tgw.evolution.util.NBTTypes;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class EvolutionServerStatisticsManager extends ServerStatisticsManager {

    protected final Object2FloatMap<Stat<?>> partialData = Object2FloatMaps.synchronize(new Object2FloatOpenHashMap<>());
    protected final Object2LongMap<Stat<?>> statsData = Object2LongMaps.synchronize(new Object2LongOpenHashMap<>());
    private final Set<Stat<?>> dirty = Sets.newHashSet();
    private final MinecraftServer server;
    private final File statsFile;
    private int lastStatRequest = -300;

    public EvolutionServerStatisticsManager(MinecraftServer server, File statsFile) {
        super(server, statsFile);
        this.server = server;
        this.statsFile = statsFile;
        this.statsData.defaultReturnValue(0);
        this.partialData.defaultReturnValue(0);
        if (statsFile.isFile()) {
            try {
                this.parseLocal(server.getFixerUpper(), FileUtils.readFileToString(statsFile));
            }
            catch (IOException exception) {
                Evolution.LOGGER.error("Couldn't read statistics file {}", statsFile, exception);
            }
            catch (JsonParseException exception) {
                Evolution.LOGGER.error("Couldn't parse statistics file {}", statsFile, exception);
            }
        }
    }

    private static CompoundNBT fromJson(JsonObject jsonObject) {
        CompoundNBT compoundNBT = new CompoundNBT();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonElement element = entry.getValue();
            if (element.isJsonObject()) {
                compoundNBT.put(entry.getKey(), fromJson(element.getAsJsonObject()));
            }
            else if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    compoundNBT.putLong(entry.getKey(), primitive.getAsLong());
                }
            }
        }
        return compoundNBT;
    }

    private static <T> ResourceLocation getKey(Stat<T> stat) {
        return stat.getType().getRegistry().getKey(stat.getValue());
    }

    private static <T> Optional<Stat<T>> getStat(StatType<T> statType, String resLoc) {
        return Optional.ofNullable(ResourceLocation.tryParse(resLoc)).flatMap(statType.getRegistry()::getOptional).map(statType::get);
    }

    private Set<Stat<?>> getDirty() {
        Set<Stat<?>> set = Sets.newHashSet(this.dirty);
        this.dirty.clear();
        return set;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T> int getValue(StatType<T> statType, T stat) {
        Evolution.LOGGER.warn("Wrong stats method, called by {}", Thread.currentThread().getStackTrace()[2]);
        return 0;
    }

    @Override
    public int getValue(Stat<?> stat) {
        Evolution.LOGGER.warn("Wrong stats method, called by {}", Thread.currentThread().getStackTrace()[2]);
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public <T> long getValueLong(StatType<T> statType, T stat) {
        return statType.contains(stat) ? this.getValueLong(statType.get(stat)) : 0;
    }

    public long getValueLong(Stat<?> stat) {
        return this.statsData.getLong(stat);
    }

    @Override
    public void increment(PlayerEntity player, Stat<?> stat, int amount) {
        if (stat.getType() == Stats.CUSTOM) {
            if ("minecraft".equals(((ResourceLocation) stat.getValue()).getNamespace())) {
                return;
            }
        }
        this.setValueLong(stat, this.getValueLong(stat) + amount);
    }

    public void incrementLong(Stat<?> stat, long amount) {
        this.setValueLong(stat, this.getValueLong(stat) + amount);
    }

    public void incrementPartial(PlayerEntity player, Stat<?> stat, float amount) {
        if (amount <= 0 || Float.isNaN(amount)) {
            return;
        }
        if (Float.isInfinite(amount)) {
            if (stat.getType() == Stats.CUSTOM) {
                if (EvolutionStats.DAMAGE_TAKEN_RAW.containsValue(stat.getValue())) {
                    amount = player.getHealth() + player.getAbsorptionAmount();
                }
                else {
                    return;
                }
            }
            else {
                return;
            }
        }
        long value = (long) amount;
        amount -= value;
        amount += this.partialData.getOrDefault(stat, 0);
        while (amount >= 1.0f) {
            amount -= 1.0f;
            value++;
        }
        this.partialData.put(stat, amount);
        if (value > 0) {
            this.incrementLong(stat, value);
        }
    }

    @Override
    public void markAllDirty() {
        this.dirty.addAll(this.statsData.keySet());
    }

    @Override
    public void parseLocal(DataFixer dataFixer, String path) {
        try (JsonReader jsonReader = new JsonReader(new StringReader(path))) {
            jsonReader.setLenient(false);
            JsonElement jsonElement = Streams.parse(jsonReader);
            if (jsonElement.isJsonNull()) {
                Evolution.LOGGER.error("Unable to parse Stat data from {}", this.statsFile);
                return;
            }
            CompoundNBT compoundNBT = fromJson(jsonElement.getAsJsonObject());
            if (!compoundNBT.contains("DataVersion", NBTTypes.ANY_NUMERIC)) {
                compoundNBT.putInt("DataVersion", 1_343);
            }
            compoundNBT = NBTUtil.update(dataFixer, DefaultTypeReferences.STATS, compoundNBT, compoundNBT.getInt("DataVersion"));
            if (compoundNBT.contains("stats", NBTTypes.COMPOUND_NBT)) {
                CompoundNBT stats = compoundNBT.getCompound("stats");
                for (String s : stats.getAllKeys()) {
                    if (stats.contains(s, NBTTypes.COMPOUND_NBT)) {
                        //noinspection ObjectAllocationInLoop
                        Util.ifElse(Registry.STAT_TYPE.getOptional(new ResourceLocation(s)), statType -> {
                            CompoundNBT compoundnbt2 = stats.getCompound(s);
                            for (String s1 : compoundnbt2.getAllKeys()) {
                                if (compoundnbt2.contains(s1, NBTTypes.ANY_NUMERIC)) {
                                    //noinspection ObjectAllocationInLoop
                                    Util.ifElse(getStat(statType, s1),
                                                stat -> this.statsData.put(stat, compoundnbt2.getLong(s1)),
                                                () -> Evolution.LOGGER.warn("Invalid statistic in {}: Don't know what {} is", this.statsFile, s1));
                                }
                                else {
                                    Evolution.LOGGER.warn("Invalid statistic value in {}: Don't know what {} is for key {}",
                                                          this.statsFile,
                                                          compoundnbt2.get(s1),
                                                          s1);
                                }
                            }
                        }, () -> Evolution.LOGGER.warn("Invalid statistic type in {}: Don't know what {} is", this.statsFile, s));
                    }
                }
            }
            if (compoundNBT.contains("partial", NBTTypes.COMPOUND_NBT)) {
                CompoundNBT partial = compoundNBT.getCompound("partial");
                for (String s : partial.getAllKeys()) {
                    if (partial.contains(s, NBTTypes.COMPOUND_NBT)) {
                        //noinspection ObjectAllocationInLoop
                        Util.ifElse(Registry.STAT_TYPE.getOptional(new ResourceLocation(s)), statType -> {
                            CompoundNBT compoundnbt2 = partial.getCompound(s);
                            for (String s1 : compoundnbt2.getAllKeys()) {
                                if (compoundnbt2.contains(s1, NBTTypes.ANY_NUMERIC)) {
                                    //noinspection ObjectAllocationInLoop
                                    Util.ifElse(getStat(statType, s1),
                                                stat -> this.partialData.put(stat, compoundnbt2.getFloat(s1)),
                                                () -> Evolution.LOGGER.warn("Invalid statistic in {}: Don't know what {} is", this.statsFile, s1));
                                }
                                else {
                                    Evolution.LOGGER.warn("Invalid statistic value in {}: Don't know what {} is for key {}",
                                                          this.statsFile,
                                                          compoundnbt2.get(s1),
                                                          s1);
                                }
                            }
                        }, () -> Evolution.LOGGER.warn("Invalid statistic type in {}: Don't know what {} is", this.statsFile, s));
                    }
                }
            }
        }
        catch (IOException | JsonParseException exception) {
            Evolution.LOGGER.error("Unable to parse Stat data from {}", this.statsFile, exception);
        }
    }

    @Override
    public void sendStats(ServerPlayerEntity player) {
        int i = this.server.getTickCount();
        Object2LongMap<Stat<?>> stats = new Object2LongOpenHashMap<>();
        if (i - this.lastStatRequest > 100) {
            this.lastStatRequest = i;
            for (Stat<?> stat : this.getDirty()) {
                stats.put(stat, this.getValueLong(stat));
            }
        }
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketSCStatistics(stats));
    }

    @Override
    public void setValue(PlayerEntity player, Stat<?> stat, int amount) {
        if (stat.getType() == Stats.CUSTOM) {
            if ("minecraft".equals(((ResourceLocation) stat.getValue()).getNamespace())) {
                return;
            }
        }
        Evolution.LOGGER.warn("Wrong stats method, called by {}", Thread.currentThread().getStackTrace()[2]);
    }

    public void setValueLong(Stat<?> stat, long amount) {
        this.statsData.put(stat, amount);
        this.dirty.add(stat);
    }

    @Override
    protected String toJson() {
        Map<StatType<?>, JsonObject> dataMap = Maps.newHashMap();
        for (Object2LongMap.Entry<Stat<?>> entry : this.statsData.object2LongEntrySet()) {
            Stat<?> stat = entry.getKey();
            //noinspection ObjectAllocationInLoop
            dataMap.computeIfAbsent(stat.getType(), key -> new JsonObject()).addProperty(getKey(stat).toString(), entry.getLongValue());
        }
        JsonObject data = new JsonObject();
        for (Map.Entry<StatType<?>, JsonObject> entry : dataMap.entrySet()) {
            data.add(Registry.STAT_TYPE.getKey(entry.getKey()).toString(), entry.getValue());
        }
        Map<StatType<?>, JsonObject> partialDataMap = Maps.newHashMap();
        for (Object2FloatMap.Entry<Stat<?>> entry : this.partialData.object2FloatEntrySet()) {
            Stat<?> stat = entry.getKey();
            //noinspection ObjectAllocationInLoop
            partialDataMap.computeIfAbsent(stat.getType(), key -> new JsonObject()).addProperty(getKey(stat).toString(), entry.getFloatValue());
        }
        JsonObject partialData = new JsonObject();
        for (Map.Entry<StatType<?>, JsonObject> entry : partialDataMap.entrySet()) {
            partialData.add(Registry.STAT_TYPE.getKey(entry.getKey()).toString(), entry.getValue());
        }
        JsonObject finalObj = new JsonObject();
        finalObj.add("stats", data);
        finalObj.add("partial", partialData);
        finalObj.addProperty("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        return finalObj.toString();
    }
}
