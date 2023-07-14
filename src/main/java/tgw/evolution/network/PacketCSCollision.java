package tgw.evolution.network;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSCollision implements Packet<ServerGamePacketListener> {

    public final Direction.Axis axis;
    public final long pos;
    public final double speed;

    public PacketCSCollision(long pos, double speed, Direction.Axis axis) {
        this.pos = pos;
        this.speed = speed;
        this.axis = axis;
    }

    public PacketCSCollision(FriendlyByteBuf buf) {
        this.pos = buf.readLong();
        this.speed = buf.readDouble();
        this.axis = buf.readEnum(Direction.Axis.class);
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleCollision(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(this.pos);
        buf.writeDouble(this.speed);
        buf.writeEnum(this.axis);
    }
}
