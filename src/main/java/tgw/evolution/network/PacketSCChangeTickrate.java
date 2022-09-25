package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCChangeTickrate implements IPacket {

    private final float tickrate;

    public PacketSCChangeTickrate(float tickrate) {
        this.tickrate = tickrate;
    }

    public static PacketSCChangeTickrate decode(FriendlyByteBuf buffer) {
        return new PacketSCChangeTickrate(buffer.readFloat());
    }

    public static void encode(PacketSCChangeTickrate packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.tickrate);
    }

    public static void handle(PacketSCChangeTickrate packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> ClientEvents.getInstance().updateClientTickrate(packet.tickrate));
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
