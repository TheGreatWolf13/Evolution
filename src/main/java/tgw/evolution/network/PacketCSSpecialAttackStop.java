package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import tgw.evolution.items.IMelee;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSSpecialAttackStop implements Packet<ServerGamePacketListener> {

    public final IMelee.StopReason reason;

    public PacketCSSpecialAttackStop(IMelee.StopReason reason) {
        this.reason = reason;
    }

    public PacketCSSpecialAttackStop(FriendlyByteBuf buf) {
        this.reason = buf.readEnum(IMelee.StopReason.class);
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleSpecialAttackStop(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.reason);
    }
}
