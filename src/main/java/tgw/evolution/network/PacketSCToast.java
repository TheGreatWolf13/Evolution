package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import tgw.evolution.patches.PatchClientPacketListener;

public class PacketSCToast implements Packet<ClientGamePacketListener> {

    public final int id;

    public PacketSCToast(int id) {
        this.id = id;
    }

    public PacketSCToast(FriendlyByteBuf buf) {
        this.id = buf.readVarInt();
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        ((PatchClientPacketListener) listener).handleToast(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.id);
    }
}
