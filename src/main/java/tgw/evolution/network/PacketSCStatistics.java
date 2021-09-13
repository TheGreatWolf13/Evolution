package tgw.evolution.network;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.Evolution;

import java.util.function.Supplier;

public class PacketSCStatistics implements IPacket {

    private final Object2LongMap<Stat<?>> statsData;

    public PacketSCStatistics(Object2LongMap<Stat<?>> statsData) {
        this.statsData = statsData;
    }

    public static PacketSCStatistics decode(PacketBuffer buffer) {
        int size = buffer.readVarInt();
        Object2LongMap<Stat<?>> statisticMap = new Object2LongOpenHashMap<>(size);
        for (int i = 0; i < size; ++i) {
            readValues(statisticMap, Registry.STAT_TYPE.byId(buffer.readVarInt()), buffer);
        }
        return new PacketSCStatistics(statisticMap);
    }

    public static void encode(PacketSCStatistics packet, PacketBuffer buffer) {
        buffer.writeVarInt(packet.statsData.size());
        for (Object2LongMap.Entry<Stat<?>> entry : packet.statsData.object2LongEntrySet()) {
            Stat<?> stat = entry.getKey();
            buffer.writeVarInt(Registry.STAT_TYPE.getId(stat.getType()));
            buffer.writeVarInt(getStatId(stat));
            buffer.writeLong(entry.getLongValue());
        }
    }

    private static <T> int getStatId(Stat<T> stat) {
        return stat.getType().getRegistry().getId(stat.getValue());
    }

    public static void handle(PacketSCStatistics packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> Evolution.PROXY.updateStats(packet.statsData));
            context.get().setPacketHandled(true);
        }
    }

    private static <T> void readValues(Object2LongMap<Stat<?>> map, StatType<T> statType, PacketBuffer buffer) {
        int statId = buffer.readVarInt();
        long amount = buffer.readLong();
        map.put(statType.get(statType.getRegistry().byId(statId)), amount);
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
