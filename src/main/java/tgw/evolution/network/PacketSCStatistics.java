package tgw.evolution.network;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.patches.PatchClientGamePacketListener;
import tgw.evolution.util.collection.maps.O2LHashMap;
import tgw.evolution.util.collection.maps.O2LMap;

public class PacketSCStatistics implements Packet<ClientGamePacketListener> {

    public final O2LMap<Stat<?>> statsData;

    public PacketSCStatistics(O2LMap<Stat<?>> statsData) {
        this.statsData = statsData;
    }

    public PacketSCStatistics(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        O2LMap<Stat<?>> statisticMap = new O2LHashMap<>(size);
        for (int i = 0; i < size; ++i) {
            readValues(statisticMap, Registry.STAT_TYPE.byId(buf.readVarInt()), buf);
        }
        this.statsData = statisticMap;
    }

    private static <T> int getStatId(Stat<T> stat) {
        return stat.getType().getRegistry().getId(stat.getValue());
    }

    private static <T> void readValues(O2LMap<Stat<?>> map, @Nullable StatType<T> statType, FriendlyByteBuf buffer) {
        int statId = buffer.readVarInt();
        long amount = buffer.readLong();
        if (statType != null) {
            T t = statType.getRegistry().byId(statId);
            assert t != null;
            map.put(statType.get(t), amount);
        }
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleStatistics(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.statsData.size());
        for (Object2LongMap.Entry<Stat<?>> entry : this.statsData.object2LongEntrySet()) {
            Stat<?> stat = entry.getKey();
            buf.writeVarInt(Registry.STAT_TYPE.getId(stat.getType()));
            buf.writeVarInt(getStatId(stat));
            buf.writeLong(entry.getLongValue());
        }
    }
}
