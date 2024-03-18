package tgw.evolution.mixin;

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
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.*;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.*;
import tgw.evolution.network.PacketSCStatistics;
import tgw.evolution.util.collection.maps.*;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;
import tgw.evolution.util.math.HalfFloat;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(ServerStatsCounter.class)
public abstract class Mixin_CFM_ServerStatsCounter extends StatsCounter {
    @Shadow @Final private static Logger LOGGER;
    @Unique private final OSet<Stat<?>> dirtyData;
    @Unique private final O2SMap<Stat<?>> partialData;
    @Shadow @Final @DeleteField private Set<Stat<?>> dirty;
    @Mutable @Shadow @Final @RestoreFinal private File file;
    @Unique private int lastStatRequest;
    @Mutable @Shadow @Final @RestoreFinal private MinecraftServer server;

    @DummyConstructor
    public Mixin_CFM_ServerStatsCounter(OSet<Stat<?>> dirtyData, O2SMap<Stat<?>> partialData) {
        this.dirtyData = dirtyData;
        this.partialData = partialData;
    }

    @ModifyConstructor
    public Mixin_CFM_ServerStatsCounter(MinecraftServer minecraftServer, File file) {
        this.partialData = new O2SHashMap<>();
        this.partialData.defaultReturnValue((short) 0);
        this.dirtyData = new OHashSet<>();
        this.server = minecraftServer;
        this.file = file;
        this.lastStatRequest = -300;
        if (file.isFile()) {
            try {
                this.parseLocal(minecraftServer.getFixerUpper(), FileUtils.readFileToString(file));
            }
            catch (IOException t) {
                LOGGER.error("Couldn't read statistics file {}", file, t);
            }
            catch (JsonParseException t) {
                LOGGER.error("Couldn't parse statistics file {}", file, t);
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
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

    @Shadow
    private static <T> ResourceLocation getKey(Stat<T> stat) {
        throw new AbstractMethodError();
    }

    @Unique
    private static @Nullable <T> Stat<T> getStat_(StatType<T> statType, String resLoc) {
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

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private Set<Stat<?>> getDirty() {
        throw new AbstractMethodError();
    }

    @Unique
    private OSet<Stat<?>> getDirtyData() {
        OSet<Stat<?>> set = new OHashSet<>(this.dirtyData);
        this.dirtyData.clear();
        return set;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private <T> Optional<Stat<T>> getStat(StatType<T> statType, String string) {
        throw new AbstractMethodError();
    }

    @Override
    public void increment(Player player, Stat<?> stat, int amount) {
        if (stat.getType() == Stats.CUSTOM) {
            if ("minecraft".equals(((ResourceLocation) stat.getValue()).getNamespace())) {
                return;
            }
        }
        super.increment(player, stat, amount);
    }

    @Override
    public void incrementPartial(Stat<?> stat, float amount) {
        if (amount <= 0 || Float.isNaN(amount)) {
            return;
        }
        if (Float.isInfinite(amount) || amount > Long.MAX_VALUE) {
            return;
        }
        synchronized (this) {
            long value = (long) amount;
            amount -= value;
            amount += HalfFloat.toFloat(this.partialData.getOrDefault(stat, (short) 0));
            while (amount >= 1.0f) {
                amount -= 1.0f;
                value++;
            }
            this.partialData.put(stat, HalfFloat.toHalf(amount));
            if (value > 0) {
                this.increment(stat, value);
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void markAllDirty() {
        this.dirtyData.addAll(this._getMap().keySet());
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void parseLocal(DataFixer dataFixer, String path) {
        synchronized (this) {
            O2LMap<Stat<?>> statsData = this._getMap();
            try (JsonReader jsonReader = new JsonReader(new StringReader(path))) {
                jsonReader.setLenient(false);
                JsonElement jsonElement = Streams.parse(jsonReader);
                if (jsonElement.isJsonNull()) {
                    Evolution.error("Unable to parse Stat data from {}", this.file);
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
                                        Stat<?> stat = getStat_(statType, key);
                                        if (stat != null) {
                                            statsData.put(stat, type.getLong(key));
                                        }
                                        else {
                                            Evolution.warn("Invalid statistic in {}: Don't know what {} is", this.file, key);
                                        }
                                    }
                                    else {
                                        Evolution.warn("Invalid statistic value in {}: Don't know what {} is for key {}", this.file, type.get(key), key);
                                    }
                                }
                            }
                            else {
                                Evolution.warn("Invalid statistic type in {}: Don't know what {} is", this.file, typeKey);
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
                                        Stat<?> stat = getStat_(statType, key);
                                        if (stat != null) {
                                            this.partialData.put(stat, type.getShort(key));
                                        }
                                        else {
                                            Evolution.warn("Invalid statistic in {}: Don't know what {} is", this.file, key);
                                        }
                                    }
                                    else {
                                        Evolution.warn("Invalid statistic value in {}: Don't know what {} is for key {}", this.file, type.get(key), key);
                                    }
                                }
                            }
                            else {
                                Evolution.warn("Invalid statistic type in {}: Don't know what {} is", this.file, typeKey);
                            }
                        }
                    }
                }
            }
            catch (IOException | JsonParseException exception) {
                Evolution.error("Unable to parse Stat data from {}", this.file, exception);
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void sendStats(ServerPlayer player) {
        int i = this.server.getTickCount();
        O2LMap<Stat<?>> stats = new O2LHashMap<>();
        if (i - this.lastStatRequest > 100) {
            this.lastStatRequest = i;
            for (Stat<?> stat : this.getDirtyData()) {
                stats.put(stat, this.getValue_(stat));
            }
        }
        player.connection.send(new PacketSCStatistics(stats));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public void setValue(Player player, Stat<?> stat, int amount) {
        if (stat.getType() == Stats.CUSTOM) {
            if ("minecraft".equals(((ResourceLocation) stat.getValue()).getNamespace())) {
                return;
            }
        }
        super.setValue(player, stat, amount);
        this.dirtyData.add(stat);
    }

    @Override
    public void setValue_(Stat<?> stat, long amount) {
        super.setValue_(stat, amount);
        this.dirtyData.add(stat);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public String toJson() {
        synchronized (this) {
            O2LMap<Stat<?>> statsData = this._getMap();
            R2OMap<StatType<?>, JsonObject> dataMap = new R2OHashMap<>();
            for (O2LMap.Entry<Stat<?>> e = statsData.fastEntries(); e != null; e = statsData.fastEntries()) {
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
