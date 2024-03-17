package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import tgw.evolution.patches.PatchClientGamePacketListener;
import tgw.evolution.util.constants.RockVariant;

public class PacketSCOpenKnappingGui implements Packet<ClientGamePacketListener> {

    public final long pos;
    public final RockVariant variant;

    public PacketSCOpenKnappingGui(long pos, RockVariant variant) {
        this.pos = pos;
        this.variant = variant;
    }

    public PacketSCOpenKnappingGui(FriendlyByteBuf buf) {
        this.pos = buf.readLong();
        this.variant = RockVariant.fromId(buf.readByte());
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleOpenKnappingGui(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(this.pos);
        buf.writeByte(this.variant.getId());
    }
}
