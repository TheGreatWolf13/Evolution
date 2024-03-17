package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import tgw.evolution.patches.PatchClientGamePacketListener;

public class PacketSCMomentum implements Packet<ClientGamePacketListener> {

    public final float x;
    public final float y;
    public final float z;

    public PacketSCMomentum(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PacketSCMomentum(FriendlyByteBuf buf) {
        this.x = buf.readFloat();
        this.y = buf.readFloat();
        this.z = buf.readFloat();
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleMomentum(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeFloat(this.x);
        buf.writeFloat(this.y);
        buf.writeFloat(this.z);
    }
}
