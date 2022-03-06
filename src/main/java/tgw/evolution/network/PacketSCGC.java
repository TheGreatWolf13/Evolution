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
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(System::gc);
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
