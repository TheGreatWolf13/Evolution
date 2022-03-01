package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;

import java.util.function.Supplier;

public class PacketCSSetProne implements IPacket {

    private final boolean proned;

    public PacketCSSetProne(boolean proned) {
        this.proned = proned;
    }

    public static PacketCSSetProne decode(FriendlyByteBuf buffer) {
        return new PacketCSSetProne(buffer.readBoolean());
    }

    public static void encode(PacketCSSetProne packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.proned);
    }

    public static void handle(PacketCSSetProne packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                Evolution.PRONED_PLAYERS.put(player.getId(), packet.proned);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
