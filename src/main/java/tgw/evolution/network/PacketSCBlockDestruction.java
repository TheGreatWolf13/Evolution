package tgw.evolution.network;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.math.DirectionUtil;

public class PacketSCBlockDestruction implements Packet<ClientGamePacketListener> {

    public final @Nullable Direction face;
    public final double hitX;
    public final double hitY;
    public final double hitZ;
    public final int id;
    public final long pos;
    public final int progress;

    public PacketSCBlockDestruction(int id, long pos, int progress, @Nullable Direction face, double hitX, double hitY, double hitZ) {
        this.id = id;
        this.pos = pos;
        this.progress = progress;
        this.face = face;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
    }

    public PacketSCBlockDestruction(FriendlyByteBuf buf) {
        this.id = buf.readVarInt();
        this.pos = buf.readLong();
        this.progress = buf.readVarInt();
        byte data = buf.readByte();
        if ((data & 1) != 0) {
            this.face = DirectionUtil.ALL[data >> 1];
            this.hitX = buf.readDouble();
            this.hitY = buf.readDouble();
            this.hitZ = buf.readDouble();
        }
        else {
            this.face = null;
            this.hitX = 0;
            this.hitY = 0;
            this.hitZ = 0;
        }
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleBlockDestruction(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.id);
        buf.writeLong(this.pos);
        buf.writeVarInt(this.progress);
        if (this.face != null) {
            buf.writeByte(this.face.ordinal() << 1 | 1);
            buf.writeDouble(this.hitX);
            buf.writeDouble(this.hitY);
            buf.writeDouble(this.hitZ);
        }
        else {
            buf.writeByte(0);
        }
    }
}
