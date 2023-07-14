package tgw.evolution.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import tgw.evolution.patches.PatchClientPacketListener;
import tgw.evolution.util.constants.RockVariant;

public class PacketSCOpenKnappingGui implements Packet<ClientGamePacketListener> {

    public final BlockPos pos;
    public final RockVariant variant;

    public PacketSCOpenKnappingGui(BlockPos pos, RockVariant variant) {
        this.pos = pos;
        this.variant = variant;
    }

    public PacketSCOpenKnappingGui(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.variant = RockVariant.fromId(buf.readByte());
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        ((PatchClientPacketListener) listener).handleOpenKnappingGui(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeByte(this.variant.getId());
    }
}
