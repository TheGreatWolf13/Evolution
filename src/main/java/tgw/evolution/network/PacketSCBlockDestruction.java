package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class PacketSCBlockDestruction implements Packet<ClientGamePacketListener> {

    public final int id;
    public final long pos;
    public final int progress;

    public PacketSCBlockDestruction(int id, long pos, int progress) {
        this.id = id;
        this.pos = pos;
        this.progress = progress;
    }

    public PacketSCBlockDestruction(FriendlyByteBuf buf) {
        this.id = buf.readVarInt();
        this.pos = buf.readLong();
        this.progress = buf.readVarInt();
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
    }
}
