package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import tgw.evolution.patches.PatchClientPacketListener;

public class PacketSCShader implements Packet<ClientGamePacketListener> {

    public static final int TOGGLE = -1;
    public static final int QUERY = -2;
    public static final int CYCLE = -3;
    public final int shaderId;

    public PacketSCShader(int shaderId) {
        this.shaderId = shaderId;
    }

    public PacketSCShader(FriendlyByteBuf buf) {
        this.shaderId = buf.readVarInt();
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        ((PatchClientPacketListener) listener).handleShader(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.shaderId);
    }
}
