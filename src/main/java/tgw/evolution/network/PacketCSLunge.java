package tgw.evolution.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.util.PlayerHelper;

import java.util.function.Supplier;

public class PacketCSLunge implements IPacket {

    private final int entityId;
    private final Hand hand;
    private final double rayTraceHeight;
    private final int slot;
    private final float strength;

    public PacketCSLunge(Entity entity, Hand hand, double rayTraceHeight, int slot, float strength) {
        this(entity == null ? -1 : entity.getId(), hand, rayTraceHeight, slot, strength);
    }

    private PacketCSLunge(int entityId, Hand hand, double rayTraceHeight, int slot, float strength) {
        this.slot = slot;
        this.strength = strength;
        this.hand = hand;
        this.rayTraceHeight = rayTraceHeight;
        this.entityId = entityId;
    }

    public static PacketCSLunge decode(PacketBuffer buffer) {
        return new PacketCSLunge(buffer.readVarInt(),
                                 buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND,
                                 buffer.readDouble(),
                                 buffer.readVarInt(),
                                 buffer.readFloat());
    }

    public static void encode(PacketCSLunge packet, PacketBuffer buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeBoolean(packet.hand == Hand.MAIN_HAND);
        buffer.writeDouble(packet.rayTraceHeight);
        buffer.writeVarInt(packet.slot);
        buffer.writeFloat(packet.strength);
    }

    public static void handle(PacketCSLunge packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = context.get().getSender();
                Entity entity = packet.entityId != -1 ? player.level.getEntity(packet.entityId) : null;
                ItemStack lungeStack = packet.slot == -1 ? player.getOffhandItem() : player.inventory.items.get(packet.slot);
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
