package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCHitmarker implements IPacket {

    private final boolean isKill;

    public PacketSCHitmarker(boolean isKill) {
        this.isKill = isKill;
    }

    public static PacketSCHitmarker decode(FriendlyByteBuf buffer) {
        return new PacketSCHitmarker(buffer.readBoolean());
    }

    public static void encode(PacketSCHitmarker packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.isKill);
    }

    public static void handle(PacketSCHitmarker packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> ClientEvents.getInstance().getRenderer().updateHitmarkers(packet.isKill));
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
