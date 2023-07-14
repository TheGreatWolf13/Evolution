package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSSimpleMessage implements Packet<ServerGamePacketListener> {

    public final Message.C2S message;

    public PacketCSSimpleMessage(Message.C2S message) {
        this.message = message;
    }

    public PacketCSSimpleMessage(FriendlyByteBuf buf) {
        this.message = buf.readEnum(Message.C2S.class);
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleSimpleMessage(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.message);
    }
}
