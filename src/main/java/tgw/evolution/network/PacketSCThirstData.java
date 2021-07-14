package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.capabilities.thirst.IThirst;
import tgw.evolution.capabilities.thirst.ThirstStats;

import java.util.function.Supplier;

public class PacketSCThirstData implements IPacket {

    private final short hydrationLevel;
    private final short thirstLevel;

    public PacketSCThirstData(ThirstStats thirst) {
        this(thirst.getThirstLevel(), thirst.getHydrationLevel());
    }

    private PacketSCThirstData(int thirstLevel, int hydrationLevel) {
        this.thirstLevel = (short) thirstLevel;
        this.hydrationLevel = (short) hydrationLevel;
    }

    public static PacketSCThirstData decode(PacketBuffer buffer) {
        return new PacketSCThirstData(buffer.readShort(), buffer.readShort());
    }

    public static void encode(PacketSCThirstData packet, PacketBuffer buffer) {
        buffer.writeShort(packet.thirstLevel);
        buffer.writeShort(packet.hydrationLevel);
    }

    public static void handle(PacketSCThirstData packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                IThirst thirst = ThirstStats.CLIENT_INSTANCE;
                thirst.setThirstLevel(packet.thirstLevel);
                thirst.setHydrationLevel(packet.hydrationLevel);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
