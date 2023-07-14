package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSPlayerFall implements Packet<ServerGamePacketListener> {

    public final double distanceOfSlowDown;
    public final double velocity;
    public final boolean water;

    public PacketCSPlayerFall(double velocity, double distanceOfSlowDown, boolean water) {
        this.velocity = velocity;
        this.distanceOfSlowDown = distanceOfSlowDown;
        this.water = water;
    }

    public PacketCSPlayerFall(FriendlyByteBuf buf) {
        this.velocity = buf.readDouble();
        this.distanceOfSlowDown = buf.readDouble();
        this.water = buf.readBoolean();
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handlePlayerFall(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(this.velocity);
        buf.writeDouble(this.distanceOfSlowDown);
        buf.writeBoolean(this.water);
    }
}
