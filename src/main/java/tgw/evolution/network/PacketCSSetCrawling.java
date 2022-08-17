package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.patches.IPlayerPatch;

import java.util.function.Supplier;

public class PacketCSSetCrawling implements IPacket {

    private final boolean crawling;

    public PacketCSSetCrawling(boolean crawling) {
        this.crawling = crawling;
    }

    public static PacketCSSetCrawling decode(FriendlyByteBuf buffer) {
        return new PacketCSSetCrawling(buffer.readBoolean());
    }

    public static void encode(PacketCSSetCrawling packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.crawling);
    }

    public static void handle(PacketCSSetCrawling packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                ((IPlayerPatch) player).setCrawling(packet.crawling);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
