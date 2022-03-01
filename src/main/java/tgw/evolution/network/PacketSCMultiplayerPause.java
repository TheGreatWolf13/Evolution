package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;

import java.util.function.Supplier;

public class PacketSCMultiplayerPause implements IPacket {

    private final boolean paused;

    public PacketSCMultiplayerPause(boolean paused) {
        this.paused = paused;
    }

    public static PacketSCMultiplayerPause decode(FriendlyByteBuf buffer) {
        return new PacketSCMultiplayerPause(buffer.readBoolean());
    }

    public static void encode(PacketSCMultiplayerPause packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.paused);
    }

    public static void handle(PacketSCMultiplayerPause packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> Evolution.PACKET_HANDLER.handleMultiplayerPause(packet.paused));
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
