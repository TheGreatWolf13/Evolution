package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.util.EntityHelper;
import tgw.evolution.util.hitbox.HitboxType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketCSHitInformation implements IPacket {

    private final InteractionHand hand;
    private final HitboxType[] hitboxes;
    private final int victimId;

    public PacketCSHitInformation(EntityAccess victim, InteractionHand hand, HitboxType... hitboxes) {
        this(victim.getId(), hand, hitboxes);
    }

    private PacketCSHitInformation(int victimId, InteractionHand hand, HitboxType... hitboxes) {
        this.hand = hand;
        this.hitboxes = hitboxes;
        this.victimId = victimId;
    }

    public static PacketCSHitInformation decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        InteractionHand hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        int length = buffer.readVarInt();
        List<HitboxType> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            list.add(buffer.readEnum(HitboxType.class));
        }
        return new PacketCSHitInformation(entityId, hand, list.toArray(HitboxType[]::new));
    }

    public static void encode(PacketCSHitInformation packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.victimId);
        buffer.writeBoolean(packet.hand == InteractionHand.MAIN_HAND);
        buffer.writeVarInt(packet.hitboxes.length);
        for (HitboxType hitbox : packet.hitboxes) {
            buffer.writeEnum(hitbox);
        }
    }

    public static void handle(PacketCSHitInformation packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                Level level = player.level;
                Entity victim = level.getEntity(packet.victimId);
                EntityHelper.attackEntity(player, victim, packet.hand, packet.hitboxes);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
