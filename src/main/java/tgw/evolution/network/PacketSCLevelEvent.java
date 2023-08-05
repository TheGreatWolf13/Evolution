package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import tgw.evolution.patches.PatchClientPacketListener;
import tgw.evolution.util.constants.LvlEvent;

public class PacketSCLevelEvent implements Packet<ClientGamePacketListener> {

    public final int data;
    public final @LvlEvent int event;
    public final boolean global;
    public final long pos;

    public PacketSCLevelEvent(@LvlEvent int event, long pos, int data, boolean global) {
        this.event = event;
        this.data = data;
        this.pos = pos;
        this.global = global;
    }

    public PacketSCLevelEvent(FriendlyByteBuf buf) {
        //noinspection MagicConstant
        this.event = buf.readVarInt();
        this.pos = buf.readLong();
        this.data = buf.readVarInt();
        this.global = buf.readBoolean();
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        ((PatchClientPacketListener) listener).handleLevelEvent(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.event);
        buf.writeLong(this.pos);
        buf.writeVarInt(this.data);
        buf.writeBoolean(this.global);
    }
}
