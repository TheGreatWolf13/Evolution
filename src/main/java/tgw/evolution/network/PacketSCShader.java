package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import tgw.evolution.patches.PatchClientGamePacketListener;

public class PacketSCShader implements Packet<ClientGamePacketListener> {

    public final int shaderId;

    public PacketSCShader(int shaderId) {
        this.shaderId = shaderId;
    }

    public PacketSCShader(FriendlyByteBuf buf) {
        this.shaderId = buf.readVarInt();
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleShader(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.shaderId);
    }
}
