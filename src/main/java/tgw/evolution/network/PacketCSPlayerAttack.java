package tgw.evolution.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.util.PlayerHelper;

import java.util.function.Supplier;

public class PacketCSPlayerAttack implements IPacket {

    private final int entityId;
    private final Hand hand;
    private final double rayTraceHeight;

    public PacketCSPlayerAttack(Entity entity, Hand hand, double rayTraceHeight) {
        this(entity != null ? entity.getId() : -1, hand, rayTraceHeight);
    }

    private PacketCSPlayerAttack(int entityId, Hand hand, double rayTraceHeight) {
        this.hand = hand;
        this.entityId = entityId;
        this.rayTraceHeight = rayTraceHeight;
    }

    public static PacketCSPlayerAttack decode(PacketBuffer buffer) {
        return new PacketCSPlayerAttack(buffer.readVarInt(), buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND, buffer.readDouble());
    }

    public static void encode(PacketCSPlayerAttack packet, PacketBuffer buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeBoolean(packet.hand == Hand.MAIN_HAND);
        buffer.writeDouble(packet.rayTraceHeight);
    }

    public static void handle(PacketCSPlayerAttack packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = context.get().getSender();
                Entity entity = packet.entityId != -1 ? player.level.getEntity(packet.entityId) : null;
                PlayerHelper.performAttack(player, entity, packet.hand, packet.rayTraceHeight);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
