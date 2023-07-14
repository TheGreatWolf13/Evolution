package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import tgw.evolution.items.IMelee;
import tgw.evolution.patches.PatchServerPacketListener;

public class PacketCSSpecialHit implements Packet<ServerGamePacketListener> {

    public final long hitboxSet;
    public final IMelee.IAttackType type;
    public final int victimId;

    public PacketCSSpecialHit(int victimId, IMelee.IAttackType type, long hitboxSet) {
        this.hitboxSet = hitboxSet;
        this.victimId = victimId;
        this.type = type;
    }

    public PacketCSSpecialHit(FriendlyByteBuf buf) {
        this.victimId = buf.readVarInt();
        this.type = IMelee.IAttackType.decode(buf);
        this.hitboxSet = buf.readLong();
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        ((PatchServerPacketListener) listener).handleSpecialHit(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.victimId);
        this.type.encode(buf);
        buf.writeLong(this.hitboxSet);
    }
}
