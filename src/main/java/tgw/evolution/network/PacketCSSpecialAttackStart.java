package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import tgw.evolution.items.IMelee;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSSpecialAttackStart implements Packet<ServerGamePacketListener> {

    public final IMelee.IAttackType type;

    public PacketCSSpecialAttackStart(IMelee.IAttackType type) {
        this.type = type;
    }

    public PacketCSSpecialAttackStart(FriendlyByteBuf buf) {
        this.type = IMelee.IAttackType.decode(buf);
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleSpecialAttackStart(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        this.type.encode(buf);
    }
}
