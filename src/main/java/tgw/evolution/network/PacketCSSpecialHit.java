package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.items.IMelee;
import tgw.evolution.util.EntityHelper;

import java.util.function.Supplier;

public class PacketCSSpecialHit implements IPacket {

    private final long hitboxSet;
    private final IMelee.IAttackType type;
    private final int victimId;

    public PacketCSSpecialHit(int victimId, IMelee.IAttackType type, long hitboxSet) {
        this.hitboxSet = hitboxSet;
        this.victimId = victimId;
        this.type = type;
    }

    public static PacketCSSpecialHit decode(FriendlyByteBuf buffer) {
        int victimId = buffer.readVarInt();
        IMelee.IAttackType type = IMelee.IAttackType.decode(buffer);
        long hitboxSet = buffer.readVarLong();
        return new PacketCSSpecialHit(victimId, type, hitboxSet);
    }

    public static void encode(PacketCSSpecialHit packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.victimId);
        packet.type.encode(buffer);
        buffer.writeVarLong(packet.hitboxSet);
    }

    public static void handle(PacketCSSpecialHit packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                ServerPlayer player = c.getSender();
                assert player != null;
                Entity victim = player.level.getEntity(packet.victimId);
                if (victim != null) {
                    EntityHelper.attackEntity(player, victim, packet.type, packet.hitboxSet);
                }
            });
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
