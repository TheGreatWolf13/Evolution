package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCToast implements IPacket {

    private final int id;

    public PacketSCToast(int id) {
        this.id = id;
    }

    public static PacketSCToast decode(FriendlyByteBuf buffer) {
        return new PacketSCToast(buffer.readVarInt());
    }

    public static void encode(PacketSCToast packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.id);
    }

    public static void handle(PacketSCToast packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> ClientEvents.getInstance().addCustomRecipeToast(packet.id));
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
