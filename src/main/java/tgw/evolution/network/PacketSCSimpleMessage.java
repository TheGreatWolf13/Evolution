package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import tgw.evolution.patches.PatchClientGamePacketListener;

public class PacketSCSimpleMessage implements Packet<ClientGamePacketListener> {

    public final Message.S2C message;

    public PacketSCSimpleMessage(Message.S2C message) {
        this.message = message;
    }

    public PacketSCSimpleMessage(FriendlyByteBuf buf) {
        this.message = buf.readEnum(Message.S2C.class);
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleSimpleMessage(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.message);
    }
}
