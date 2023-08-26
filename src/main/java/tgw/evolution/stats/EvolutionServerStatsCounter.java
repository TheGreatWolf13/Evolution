package tgw.evolution.stats;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.network.PacketSCStatistics;
import tgw.evolution.util.collection.maps.*;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;
import tgw.evolution.util.constants.NBTType;
import tgw.evolution.util.math.HalfFloat;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;

public class EvolutionServerStatsCounter extends ServerStatsCounter {

    protected final O2SMap<Stat<?>> partialData = new O2SHashMap<>();
    protected final O2LMap<Stat<?>> statsData = new O2LHashMap<>();
    private final OSet<Stat<?>> dirty = new OHashSet<>();
    private final MinecraftServer server;
    private final File statsFile;
    private int lastStatRequest = -300;

    public EvolutionServerStatsCounter(MinecraftServer server, File statsFile) {
        super(server, statsFile);
        this.server = server;
        this.statsFile = statsFile;
        this.statsData.defaultReturnValue(0L);
        this.partialData.defaultReturnValue((short) 0);
        if (statsFile.isFile()) {
            try {
                this.parseLocal(server.getFixerUpper(), FileUtils.readFileToString(statsFile));
            }
            catch (IOException exception) {
                Evolution.error("Couldn't read statistics file {}", statsFile, exception);
            }
            catch (JsonParseException exception) {
                Evolution.error("Couldn't parse statistics file {}", statsFile, exception);
            }
        }
    }

    private static CompoundTag fromJson(JsonObject jsonObject) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonElement element = entry.getValue();
            if (element.isJsonObject()) {
                tag.put(entry.getKey(), fromJson(element.getAsJsonObject()));
            }
            else if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    tag.putLong(entry.getKey(), primitive.getAsLong());
                }
            }
        }
        return tag;
    }

    private static <T> ResourceLocation getKey(Stat<T> stat) {
        //noinspection ConstantConditions
        return stat.getType().getRegistry().getKey(stat.getValue());
    }

    private static @Nullable <T> Stat<T> getStat(StatType<T> statType, String resLoc) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(resLoc);
        if (resourceLocation == null) {
            return null;
        }
        T t = statType.getRegistry().get(resourceLocation);
        if (t == null) {
            return null;
        }
        return statType.get(t);
    }

    private Set<Stat<?>> getDirty() {
        OSet<Stat<?>> set = new OHashSet<>(this.dirty);
        this.dirty.clear();
        return set;
    }

    @Override
    public <T> int getValue(StatType<T> statType, T stat) {
        Evolution.warn("Wrong stats method, called by {}", Thread.currentThread().getStackTrace()[2]);
        return 0;
    }

    @Override
    public int getValue(Stat<?> stat) {
        Evolution.warn("Wrong stats method, called by {}", Thread.currentThread().getStackTrace()[2]);
        return 0;
    }

    public <T> long getValueLong(StatType<T> statType, T stat) {
        return statType.contains(stat) ? this.getValueLong(statType.get(stat)) : 0;
    }

    public long getValueLong(Stat<?> stat) {
        synchronized (this.statsData) {
            return this.statsData.getLong(stat);
        }
    }

    @Override
    public void increment(Player player, Stat<?> stat, int amount) {
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

    public void incrementPartial(Player player, Stat<?> stat, float amount) {
        if (amount <= 0 || Float.isNaN(amount)) {
            return;
        }
        if (Float.isInfinite(amount) || amount > Long.MAX_VALUE) {
            if (stat.getType() == Stats.CUSTOM) {
                return;
            }
            return;
        }
        synchronized (this.statsData) {
            long value = (long) amount;
            amount -= value;
            amount += HalfFloat.toFloat(this.partialData.getOrDefault(stat, (short) 0));
            while (amount >= 1.0f) {
                amount -= 1.0f;
                value++;
            }
            this.partialData.put(stat, HalfFloat.toHalf(amount));
            if (value > 0) {
                this.incrementLong(stat, value);
            }
        }
    }

    @Override
    public void markAllDirty() {
        synchronized (this.statsData) {
            this.dirty.addAll(this.statsData.keySet());
        }
    }

    @Override
    public void parseLocal(DataFixer dataFixer, String path) {
        synchronized (this.statsData) {
            try (JsonReader jsonReader = new JsonReader(new StringReader(path))) {
                jsonReader.setLenient(false);
                JsonElement jsonElement = Streams.parse(jsonReader);
                if (jsonElement.isJsonNull()) {
                    Evolution.error("Unable to parse Stat data from {}", this.statsFile);
                    return;
                }
                CompoundTag tag = fromJson(jsonElement.getAsJsonObject());
                if (!tag.contains("DataVersion", NBTType.ANY_NUMERIC)) {
                    tag.putInt("DataVersion", 1_343);
                }
                tag = NbtUtils.update(dataFixer, DataFixTypes.STATS, tag, tag.getInt("DataVersion"));
                if (tag.contains("stats", NBTType.COMPOUND)) {
                    CompoundTag stats = tag.getCompound("stats");
                    for (String typeKey : stats.getAllKeys()) {
                        if (stats.contains(typeKey, NBTType.COMPOUND)) {
                            //noinspection ObjectAllocationInLoop
                            StatType<?> statType = Registry.STAT_TYPE.get(new ResourceLocation(typeKey));
                            if (statType != null) {
                                CompoundTag type = stats.getCompound(typeKey);
                                for (String key : type.getAllKeys()) {
                                    if (type.contains(key, NBTType.ANY_NUMERIC)) {
                                        Stat<?> stat = getStat(statType, key);
                                        if (stat != null) {
                                            this.statsData.put(stat, type.getLong(key));
                                        }
                                        else {
                                            Evolution.warn("Invalid statistic in {}: Don't know what {} is", this.statsFile, key);
                                        }
                                    }
                                    else {
                                        Evolution.warn("Invalid statistic value in {}: Don't know what {} is for key {}", this.statsFile, type.get(key), key);
                                    }
                                }
                            }
                            else {
                                Evolution.warn("Invalid statistic type in {}: Don't know what {} is", this.statsFile, typeKey);
                            }
                        }
                    }
                }
                if (tag.contains("partial", NBTType.COMPOUND)) {
                    CompoundTag partial = tag.getCompound("partial");
                    for (String typeKey : partial.getAllKeys()) {
                        if (partial.contains(typeKey, NBTType.COMPOUND)) {
                            //noinspection ObjectAllocationInLoop
                            StatType<?> statType = Registry.STAT_TYPE.get(new ResourceLocation(typeKey));
                            if (statType != null) {
                                CompoundTag type = partial.getCompound(typeKey);
                                for (String key : type.getAllKeys()) {
                                    if (type.contains(key, NBTType.ANY_NUMERIC)) {
                                        Stat<?> stat = getStat(statType, key);
                                        if (stat != null) {
                                            this.partialData.put(stat, type.getShort(key));
                                        }
                                        else {
                                            Evolution.warn("Invalid statistic in {}: Don't know what {} is", this.statsFile, key);
                                        }
                                    }
                                    else {
                                        Evolution.warn("Invalid statistic value in {}: Don't know what {} is for key {}", this.statsFile, type.get(key), key);
                                    }
                                }
                            }
                            else {
                                Evolution.warn("Invalid statistic type in {}: Don't know what {} is", this.statsFile, typeKey);
                            }
                        }
                    }
                }
            }
            catch (IOException | JsonParseException exception) {
                Evolution.error("Unable to parse Stat data from {}", this.statsFile, exception);
            }
        }
    }

    @Override
    public void sendStats(ServerPlayer player) {
        int i = this.server.getTickCount();
        O2LMap<Stat<?>> stats = new O2LHashMap<>();
        if (i - this.lastStatRequest > 100) {
            this.lastStatRequest = i;
            for (Stat<?> stat : this.getDirty()) {
                stats.put(stat, this.getValueLong(stat));
            }
        }
        player.connection.send(new PacketSCStatistics(stats));
    }

    @Override
    public void setValue(Player player, Stat<?> stat, int amount) {
        if (stat.getType() == Stats.CUSTOM) {
            if ("minecraft".equals(((ResourceLocation) stat.getValue()).getNamespace())) {
                return;
            }
        }
        Evolution.warn("Wrong stats method, called by {}", Thread.currentThread().getStackTrace()[2]);
    }

    public void setValueLong(Stat<?> stat, long amount) {
        synchronized (this.statsData) {
            this.statsData.put(stat, amount);
        }
        this.dirty.add(stat);
    }

    @Override
    protected String toJson() {
        synchronized (this.statsData) {
            R2OMap<StatType<?>, JsonObject> dataMap = new R2OHashMap<>();
            for (O2LMap.Entry<Stat<?>> e = this.statsData.fastEntries(); e != null; e = this.statsData.fastEntries()) {
                Stat<?> stat = e.key();
                JsonObject json = dataMap.get(stat.getType());
                if (json == null) {
                    json = new JsonObject();
                    dataMap.put(stat.getType(), json);
                }
                json.addProperty(getKey(stat).toString(), e.value());
            }
            JsonObject data = new JsonObject();
            for (R2OMap.Entry<StatType<?>, JsonObject> e = dataMap.fastEntries(); e != null; e = dataMap.fastEntries()) {
                //noinspection ConstantConditions
                data.add(Registry.STAT_TYPE.getKey(e.key()).toString(), e.value());
            }
            R2OMap<StatType<?>, JsonObject> partialDataMap = new R2OHashMap<>();
            for (O2SMap.Entry<Stat<?>> e = this.partialData.fastEntries(); e != null; e = this.partialData.fastEntries()) {
                Stat<?> stat = e.key();
                JsonObject json = partialDataMap.get(stat.getType());
                if (json == null) {
                    json = new JsonObject();
                    partialDataMap.put(stat.getType(), json);
                }
                json.addProperty(getKey(stat).toString(), e.value());
            }
            JsonObject partialData = new JsonObject();
            for (R2OMap.Entry<StatType<?>, JsonObject> e = partialDataMap.fastEntries(); e != null; e = partialDataMap.fastEntries()) {
                //noinspection ConstantConditions
                partialData.add(Registry.STAT_TYPE.getKey(e.key()).toString(), e.value());
            }
            JsonObject finalObj = new JsonObject();
            finalObj.add("stats", data);
            finalObj.add("partial", partialData);
            finalObj.addProperty("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
            return finalObj.toString();
        }
    }
}
