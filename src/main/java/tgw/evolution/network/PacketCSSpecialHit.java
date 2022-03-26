package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.util.EntityHelper;
import tgw.evolution.util.hitbox.HitboxType;

import java.util.function.Supplier;

public class PacketCSSpecialHit implements IPacket {

    private final InteractionHand hand;
    private final HitboxType[] hitboxes;
    private final int victimId;

    public PacketCSSpecialHit(int victimId, InteractionHand hand, HitboxType... hitboxes) {
        this.hand = hand;
        this.hitboxes = hitboxes;
        this.victimId = victimId;
    }

    public static PacketCSSpecialHit decode(FriendlyByteBuf buffer) {
        int victimId = buffer.readVarInt();
        InteractionHand hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        int length = buffer.readVarInt();
        HitboxType[] hitboxes = new HitboxType[length];
        for (int i = 0; i < length; i++) {
            hitboxes[i] = buffer.readEnum(HitboxType.class);
        }
        return new PacketCSSpecialHit(victimId, hand, hitboxes);
    }

    public static void encode(PacketCSSpecialHit packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.victimId);
        buffer.writeBoolean(packet.hand == InteractionHand.MAIN_HAND);
        buffer.writeVarInt(packet.hitboxes.length);
        for (HitboxType hitbox : packet.hitboxes) {
            buffer.writeEnum(hitbox);
        }
    }

    public static void handle(PacketCSSpecialHit packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                Entity victim = player.level.getEntity(packet.victimId);
                if (victim != null) {
                    EntityHelper.attackEntity(player, victim, packet.hand, packet.hitboxes);
                }
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
