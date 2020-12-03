package tgw.evolution.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.Evolution;

import java.util.function.Supplier;

public class PacketCSSetProne implements IPacket {

    private final boolean proned;

    public PacketCSSetProne(boolean proned) {
        this.proned = proned;
    }

    public static PacketCSSetProne decode(PacketBuffer buffer) {
        return new PacketCSSetProne(buffer.readBoolean());
    }

    public static void encode(PacketCSSetProne packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.proned);
    }

    public static void handle(PacketCSSetProne packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = context.get().getSender();
                Evolution.PRONED_PLAYERS.put(player.getUniqueID(), packet.proned);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
