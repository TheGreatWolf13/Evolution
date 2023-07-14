package tgw.evolution.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import tgw.evolution.blocks.tileentities.EnumMolding;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSSetMoldingType implements Packet<ServerGamePacketListener> {

    public final EnumMolding molding;
    public final BlockPos pos;

    public PacketCSSetMoldingType(BlockPos pos, EnumMolding molding) {
        this.pos = pos;
        this.molding = molding;
    }

    public PacketCSSetMoldingType(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.molding = EnumMolding.byId(buf.readByte());
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleSetMoldingType(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeByte(this.molding.getId());
    }
}
