package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import tgw.evolution.blocks.tileentities.KnappingRecipe;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSSetKnappingType implements Packet<ServerGamePacketListener> {

    public final long pos;
    public final KnappingRecipe type;

    public PacketCSSetKnappingType(long pos, KnappingRecipe type) {
        this.pos = pos;
        this.type = type;
    }

    public PacketCSSetKnappingType(FriendlyByteBuf buf) {
        this.pos = buf.readLong();
        this.type = KnappingRecipe.byId(buf.readByte());
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleSetKnappingType(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(this.pos);
        buf.writeByte(this.type.getId());
    }
}
