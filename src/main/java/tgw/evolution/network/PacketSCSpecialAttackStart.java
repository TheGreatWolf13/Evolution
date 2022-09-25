package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.items.IMelee;
import tgw.evolution.patches.ILivingEntityPatch;

import java.util.function.Supplier;

public class PacketSCSpecialAttackStart implements IPacket {

    private final int id;
    private final IMelee.IAttackType type;

    public PacketSCSpecialAttackStart(LivingEntity entity, IMelee.IAttackType type) {
        this.type = type;
        this.id = entity.getId();
    }

    private PacketSCSpecialAttackStart(int id, IMelee.IAttackType type) {
        this.type = type;
        this.id = id;
    }

    public static PacketSCSpecialAttackStart decode(FriendlyByteBuf buf) {
        return new PacketSCSpecialAttackStart(buf.readVarInt(), IMelee.IAttackType.decode(buf));
    }

    public static void encode(PacketSCSpecialAttackStart packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.id);
        packet.type.encode(buf);
    }

    public static void handle(PacketSCSpecialAttackStart packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                Level level = Evolution.PROXY.getClientLevel();
                Entity entity = level.getEntity(packet.id);
                if (entity instanceof ILivingEntityPatch living) {
                    living.startSpecialAttack(packet.type);
                }
                else {
                    Evolution.warn("Received PacketSCSpecialAttackStart on an invalid Entity: {}", entity);
                }
            });
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
