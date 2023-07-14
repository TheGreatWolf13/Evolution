package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSImpactDamage implements Packet<ServerGamePacketListener> {

    public final float damage;

    public PacketCSImpactDamage(float damage) {
        this.damage = damage;
    }

    public PacketCSImpactDamage(FriendlyByteBuf buf) {
        this.damage = buf.readFloat();
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleImpactDamage(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeFloat(this.damage);
    }
}
