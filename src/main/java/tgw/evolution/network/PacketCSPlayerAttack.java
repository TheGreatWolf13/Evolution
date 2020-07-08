package tgw.evolution.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.util.PlayerHelper;

import java.util.function.Supplier;

public class PacketCSPlayerAttack extends PacketAbstract {

    private final Hand hand;
    private final int entityId;

    public PacketCSPlayerAttack(Entity entity, Hand hand) {
        super(LogicalSide.SERVER);
        this.hand = hand;
        this.entityId = entity != null ? entity.getEntityId() : -1;
    }

    private PacketCSPlayerAttack(int entityId, Hand hand) {
        super(LogicalSide.SERVER);
        this.hand = hand;
        this.entityId = entityId;
    }

    public static void encode(PacketCSPlayerAttack packet, PacketBuffer buffer) {
        buffer.writeInt(packet.entityId);
        buffer.writeBoolean(packet.hand == Hand.MAIN_HAND);
    }

    public static PacketCSPlayerAttack decode(PacketBuffer buffer) {
        return new PacketCSPlayerAttack(buffer.readInt(), buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND);
    }

    public static void handle(PacketCSPlayerAttack packet, Supplier<NetworkEvent.Context> context) {
        if (EvolutionNetwork.checkSide(context, packet)) {
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = context.get().getSender();
                Entity entity = packet.entityId != -1 ? player.world.getEntityByID(packet.entityId) : null;
                PlayerHelper.performAttack(player, entity, packet.hand);
            });
            context.get().setPacketHandled(true);
        }
    }
}
