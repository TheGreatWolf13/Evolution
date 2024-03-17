package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.LivingEntity;
import tgw.evolution.items.IMelee;

public class PacketSCSpecialAttackStop implements Packet<ClientGamePacketListener> {

    public final int id;
    public final IMelee.StopReason reason;

    public PacketSCSpecialAttackStop(FriendlyByteBuf buf) {
        this.id = buf.readVarInt();
        this.reason = buf.readEnum(IMelee.StopReason.class);
    }

    public PacketSCSpecialAttackStop(LivingEntity entity, IMelee.StopReason reason) {
        this.id = entity.getId();
        this.reason = reason;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleSpecialAttackStop(this);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.id);
        buf.writeEnum(this.reason);
    }
}
