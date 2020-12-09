package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCChangeTickrate implements IPacket {

    private final float tickrate;

    public PacketSCChangeTickrate(float tickrate) {
        this.tickrate = tickrate;
    }

    public static PacketSCChangeTickrate decode(PacketBuffer buffer) {
        return new PacketSCChangeTickrate(buffer.readFloat());
    }

    public static void encode(PacketSCChangeTickrate packet, PacketBuffer buffer) {
        buffer.writeFloat(packet.tickrate);
    }

    public static void handle(PacketSCChangeTickrate packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> ClientEvents.getInstance().updateClientTickrate(packet.tickrate));
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
