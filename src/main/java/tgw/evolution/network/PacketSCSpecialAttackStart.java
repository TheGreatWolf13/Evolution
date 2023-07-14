package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.LivingEntity;
import tgw.evolution.items.IMelee;
import tgw.evolution.patches.PatchClientPacketListener;

public class PacketSCSpecialAttackStart implements Packet<ClientGamePacketListener> {

    public final int id;
    public final IMelee.IAttackType type;

    public PacketSCSpecialAttackStart(LivingEntity entity, IMelee.IAttackType type) {
        this.type = type;
        this.id = entity.getId();
    }

    public PacketSCSpecialAttackStart(FriendlyByteBuf buf) {
        this.id = buf.readVarInt();
        this.type = IMelee.IAttackType.decode(buf);
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        ((PatchClientPacketListener) listener).handleSpecialAttackStart(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.id);
        this.type.encode(buf);
    }
}
