package tgw.evolution.stats;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.network.PacketSCStatistics;
import tgw.evolution.util.math.HalfFloat;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;

public class EvolutionServerStatsCounter extends ServerStatsCounter {

    protected final Object2ShortMap<Stat<?>> partialData = Object2ShortMaps.synchronize(new Object2ShortOpenHashMap<>());
    protected final Object2LongMap<Stat<?>> statsData = Object2LongMaps.synchronize(new Object2LongOpenHashMap<>());
    private final ObjectSet<Stat<?>> dirty = new ObjectOpenHashSet<>();
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
        return stat.getType().getRegistry().getKey(stat.getValue());
    }

    @Nullable
    private static <T> Stat<T> getStat(StatType<T> statType, String resLoc) {
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
        ObjectSet<Stat<?>> set = new ObjectOpenHashSet<>(this.dirty);
        this.dirty.clear();
        return set;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T> int getValue(StatType<T> statType, T stat) {
        Evolution.warn("Wrong stats method, called by {}", Thread.currentThread().getStackTrace()[2]);
        return 0;
    }

    @Override
    public int getValue(Stat<?> stat) {
        Evolution.warn("Wrong stats method, called by {}", Thread.currentThread().getStackTrace()[2]);
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
                Evolution.error("Unable to parse Stat data from {}", this.statsFile);
                return;
            }
            CompoundTag tag = fromJson(jsonElement.getAsJsonObject());
            if (!tag.contains("DataVersion", Tag.TAG_ANY_NUMERIC)) {
                tag.putInt("DataVersion", 1_343);
            }
            tag = NbtUtils.update(dataFixer, DataFixTypes.STATS, tag, tag.getInt("DataVersion"));
            if (tag.contains("stats", Tag.TAG_COMPOUND)) {
                CompoundTag stats = tag.getCompound("stats");
                for (String typeKey : stats.getAllKeys()) {
                    if (stats.contains(typeKey, Tag.TAG_COMPOUND)) {
                        //noinspection ObjectAllocationInLoop
                        StatType<?> statType = Registry.STAT_TYPE.get(new ResourceLocation(typeKey));
                        if (statType != null) {
                            CompoundTag type = stats.getCompound(typeKey);
                            for (String key : type.getAllKeys()) {
                                if (type.contains(key, Tag.TAG_ANY_NUMERIC)) {
                                    Stat<?> stat = getStat(statType, key);
                                    if (stat != null) {
                                        this.statsData.put(stat, type.getLong(key));
                                    }
                                    else {
                                        Evolution.warn("Invalid statistic in {}: Don't know what {} is", this.statsFile, key);
                                    }
                                }
                                else {
                                    Evolution.warn("Invalid statistic value in {}: Don't know what {} is for key {}", this.statsFile, type.get(key),
                                                   key);
                                }
                            }
                        }
                        else {
                            Evolution.warn("Invalid statistic type in {}: Don't know what {} is", this.statsFile, typeKey);
                        }
                    }
                }
            }
            if (tag.contains("partial", Tag.TAG_COMPOUND)) {
                CompoundTag partial = tag.getCompound("partial");
                for (String typeKey : partial.getAllKeys()) {
                    if (partial.contains(typeKey, Tag.TAG_COMPOUND)) {
                        //noinspection ObjectAllocationInLoop
                        StatType<?> statType = Registry.STAT_TYPE.get(new ResourceLocation(typeKey));
                        if (statType != null) {
                            CompoundTag type = partial.getCompound(typeKey);
                            for (String key : type.getAllKeys()) {
                                if (type.contains(key, Tag.TAG_ANY_NUMERIC)) {
                                    Stat<?> stat = getStat(statType, key);
                                    if (stat != null) {
                                        this.partialData.put(stat, type.getShort(key));
                                    }
                                    else {
                                        Evolution.warn("Invalid statistic in {}: Don't know what {} is", this.statsFile, key);
                                    }
                                }
                                else {
                                    Evolution.warn("Invalid statistic value in {}: Don't know what {} is for key {}", this.statsFile, type.get(key),
                                                   key);
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

    @Override
    public void sendStats(ServerPlayer player) {
        int i = this.server.getTickCount();
        Object2LongMap<Stat<?>> stats = new Object2LongOpenHashMap<>();
        if (i - this.lastStatRequest > 100) {
            this.lastStatRequest = i;
            for (Stat<?> stat : this.getDirty()) {
                stats.put(stat, this.getValueLong(stat));
            }
        }
        EvolutionNetwork.send(player, new PacketSCStatistics(stats));
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
        this.statsData.put(stat, amount);
        this.dirty.add(stat);
    }

    @Override
    protected String toJson() {
        Reference2ObjectMap<StatType<?>, JsonObject> dataMap = new Reference2ObjectOpenHashMap<>();
        for (Object2LongMap.Entry<Stat<?>> entry : this.statsData.object2LongEntrySet()) {
            Stat<?> stat = entry.getKey();
            JsonObject json = dataMap.get(stat.getType());
            if (json == null) {
                json = new JsonObject();
                dataMap.put(stat.getType(), json);
            }
            json.addProperty(getKey(stat).toString(), entry.getLongValue());
        }
        JsonObject data = new JsonObject();
        for (Reference2ObjectMap.Entry<StatType<?>, JsonObject> entry : dataMap.reference2ObjectEntrySet()) {
            data.add(Registry.STAT_TYPE.getKey(entry.getKey()).toString(), entry.getValue());
        }
        Reference2ObjectMap<StatType<?>, JsonObject> partialDataMap = new Reference2ObjectOpenHashMap<>();
        for (Object2ShortMap.Entry<Stat<?>> entry : this.partialData.object2ShortEntrySet()) {
            Stat<?> stat = entry.getKey();
            JsonObject json = partialDataMap.get(stat.getType());
            if (json == null) {
                json = new JsonObject();
                partialDataMap.put(stat.getType(), json);
            }
            json.addProperty(getKey(stat).toString(), entry.getShortValue());
        }
        JsonObject partialData = new JsonObject();
        for (Reference2ObjectMap.Entry<StatType<?>, JsonObject> entry : partialDataMap.reference2ObjectEntrySet()) {
            partialData.add(Registry.STAT_TYPE.getKey(entry.getKey()).toString(), entry.getValue());
        }
        JsonObject finalObj = new JsonObject();
        finalObj.add("stats", data);
        finalObj.add("partial", partialData);
        finalObj.addProperty("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        return finalObj.toString();
    }
}
