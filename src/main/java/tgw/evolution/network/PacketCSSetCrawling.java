package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSSetCrawling implements Packet<ServerGamePacketListener> {

    public final boolean crawling;

    public PacketCSSetCrawling(boolean crawling) {
        this.crawling = crawling;
    }

    public PacketCSSetCrawling(FriendlyByteBuf buf) {
        this.crawling = buf.readBoolean();
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleSetCrawling(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.crawling);
    }
}
