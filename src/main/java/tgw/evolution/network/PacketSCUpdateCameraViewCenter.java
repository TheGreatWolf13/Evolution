package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class PacketSCUpdateCameraViewCenter implements Packet<ClientGamePacketListener> {

    public final int camX;
    public final int camZ;

    public PacketSCUpdateCameraViewCenter(int camX, int camZ) {
        this.camX = camX;
        this.camZ = camZ;
    }

    public PacketSCUpdateCameraViewCenter(FriendlyByteBuf buf) {
        this.camX = buf.readVarInt();
        this.camZ = buf.readVarInt();
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleUpdateCameraViewCenter(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.camX);
        buf.writeVarInt(this.camZ);
    }
}
