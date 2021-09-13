package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCHitmarker implements IPacket {

    private final boolean isKill;

    public PacketSCHitmarker(boolean isKill) {
        this.isKill = isKill;
    }

    public static PacketSCHitmarker decode(PacketBuffer buffer) {
        return new PacketSCHitmarker(buffer.readBoolean());
    }

    public static void encode(PacketSCHitmarker packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.isKill);
    }

    public static void handle(PacketSCHitmarker packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> ClientEvents.getInstance().getRenderer().updateHitmarkers(packet.isKill));
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
