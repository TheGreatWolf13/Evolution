package tgw.evolution.network;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSPlayerAction implements Packet<ServerGamePacketListener> {

    public final ServerboundPlayerActionPacket.Action action;
    public final Direction direction;
    public final long pos;
    public final double x;
    public final double y;
    public final double z;

    public PacketCSPlayerAction(ServerboundPlayerActionPacket.Action action, long pos, Direction direction, double x, double y, double z) {
        this.pos = pos;
        this.direction = direction;
        this.action = action;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PacketCSPlayerAction(FriendlyByteBuf buf) {
        this.pos = buf.readLong();
        this.direction = buf.readEnum(Direction.class);
        this.action = buf.readEnum(ServerboundPlayerActionPacket.Action.class);
        if (this.action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            this.x = buf.readDouble();
            this.y = buf.readDouble();
            this.z = buf.readDouble();
        }
        else {
            this.x = Double.NaN;
            this.y = Double.NaN;
            this.z = Double.NaN;
        }
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handlePlayerAction(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(this.pos);
        buf.writeEnum(this.direction);
        buf.writeEnum(this.action);
        boolean validHit = !Double.isNaN(this.x);
        assert this.action != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK || validHit : "Invalid hit position";
        if (validHit) {
            buf.writeDouble(this.x);
            buf.writeDouble(this.y);
            buf.writeDouble(this.z);
        }
    }
}
