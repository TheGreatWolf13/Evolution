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

public class PacketSCSpecialAttackStop implements IPacket {

    private final int id;
    private final IMelee.StopReason reason;

    private PacketSCSpecialAttackStop(int id, IMelee.StopReason reason) {
        this.id = id;
        this.reason = reason;
    }

    public PacketSCSpecialAttackStop(LivingEntity entity, IMelee.StopReason reason) {
        this(entity.getId(), reason);
    }

    public static PacketSCSpecialAttackStop decode(FriendlyByteBuf buf) {
        return new PacketSCSpecialAttackStop(buf.readVarInt(), buf.readEnum(IMelee.StopReason.class));
    }

    public static void encode(PacketSCSpecialAttackStop packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.id);
        buf.writeEnum(packet.reason);
    }

    public static void handle(PacketSCSpecialAttackStop packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                Level level = Evolution.PROXY.getClientLevel();
                Entity entity = level.getEntity(packet.id);
                if (entity instanceof ILivingEntityPatch living) {
                    living.stopSpecialAttack(packet.reason);
                }
                else {
                    Evolution.warn("Received PacketSCSpecialAttackStop on an invalid Entity: {}", entity);
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
