package tgw.evolution.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class PacketSCOpenMoldingGui implements Packet<ClientGamePacketListener> {

    public final BlockPos pos;

    public PacketSCOpenMoldingGui(BlockPos pos) {
        this.pos = pos;
    }

    public PacketSCOpenMoldingGui(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleOpenMoldingGui(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }
}
