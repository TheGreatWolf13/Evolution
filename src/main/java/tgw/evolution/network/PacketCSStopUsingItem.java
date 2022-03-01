package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCSStopUsingItem implements IPacket {

    public static PacketCSStopUsingItem decode(FriendlyByteBuf buffer) {
        return new PacketCSStopUsingItem();
    }

    public static void encode(PacketCSStopUsingItem packet, FriendlyByteBuf buffer) {
    }

    public static void handle(IPacket packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                player.stopUsingItem();
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
