package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.util.PlayerHelper;

import java.util.function.Supplier;

public class PacketCSPlayerAttack implements IPacket {

    private final int entityId;
    private final InteractionHand hand;
    private final double rayTraceHeight;

    public PacketCSPlayerAttack(EntityAccess entity, InteractionHand hand, double rayTraceHeight) {
        this(entity.getId(), hand, rayTraceHeight);
    }

    private PacketCSPlayerAttack(int entityId, InteractionHand hand, double rayTraceHeight) {
        this.hand = hand;
        this.entityId = entityId;
        this.rayTraceHeight = rayTraceHeight;
    }

    public static PacketCSPlayerAttack decode(FriendlyByteBuf buffer) {
        return new PacketCSPlayerAttack(buffer.readVarInt(),
                                        buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                                        buffer.readDouble());
    }

    public static void encode(PacketCSPlayerAttack packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeBoolean(packet.hand == InteractionHand.MAIN_HAND);
        buffer.writeDouble(packet.rayTraceHeight);
    }

    public static void handle(PacketCSPlayerAttack packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                ServerPlayer player = c.getSender();
                assert player != null;
                Entity entity = packet.entityId != -1 ? player.level.getEntity(packet.entityId) : null;
                PlayerHelper.performAttack(player, entity, packet.hand, packet.rayTraceHeight);
            });
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
