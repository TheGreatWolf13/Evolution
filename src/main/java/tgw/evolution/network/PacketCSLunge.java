package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.util.PlayerHelper;

import java.util.function.Supplier;

public class PacketCSLunge implements IPacket {

    private final int entityId;
    private final InteractionHand hand;
    private final double rayTraceHeight;
    private final int slot;
    private final float strength;

    public PacketCSLunge(EntityAccess entity, InteractionHand hand, double rayTraceHeight, int slot, float strength) {
        this(entity == null ? -1 : entity.getId(), hand, rayTraceHeight, slot, strength);
    }

    private PacketCSLunge(int entityId, InteractionHand hand, double rayTraceHeight, int slot, float strength) {
        this.slot = slot;
        this.strength = strength;
        this.hand = hand;
        this.rayTraceHeight = rayTraceHeight;
        this.entityId = entityId;
    }

    public static PacketCSLunge decode(FriendlyByteBuf buffer) {
        return new PacketCSLunge(buffer.readVarInt(),
                                 buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                                 buffer.readDouble(),
                                 buffer.readVarInt(),
                                 buffer.readFloat());
    }

    public static void encode(PacketCSLunge packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeBoolean(packet.hand == InteractionHand.MAIN_HAND);
        buffer.writeDouble(packet.rayTraceHeight);
        buffer.writeVarInt(packet.slot);
        buffer.writeFloat(packet.strength);
    }

    public static void handle(PacketCSLunge packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                Entity entity = packet.entityId != -1 ? player.level.getEntity(packet.entityId) : null;
                ItemStack lungeStack = packet.slot == -1 ? player.getOffhandItem() : player.getInventory().items.get(packet.slot);
                PlayerHelper.performLunge(player, entity, packet.hand, packet.rayTraceHeight, lungeStack, packet.strength);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
