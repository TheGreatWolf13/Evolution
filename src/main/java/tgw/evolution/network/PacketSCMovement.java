package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class PacketSCMovement implements Packet<ClientGamePacketListener> {

    public final double motionX;
    public final double motionY;
    public final double motionZ;

    public PacketSCMovement(double motionX, double motionY, double motionZ) {
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }

    public PacketSCMovement(FriendlyByteBuf buf) {
        this.motionX = buf.readDouble();
        this.motionY = buf.readDouble();
        this.motionZ = buf.readDouble();
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleMovement(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(this.motionX);
        buf.writeDouble(this.motionY);
        buf.writeDouble(this.motionZ);
    }
}
