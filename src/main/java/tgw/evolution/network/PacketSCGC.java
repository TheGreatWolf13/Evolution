package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSCGC implements IPacket {

    public static PacketSCGC decode(FriendlyByteBuf buffer) {
        return new PacketSCGC();
    }

    public static void encode(PacketSCGC packet, FriendlyByteBuf buffer) {

    }

    public static void handle(PacketSCGC packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(System::gc);
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
