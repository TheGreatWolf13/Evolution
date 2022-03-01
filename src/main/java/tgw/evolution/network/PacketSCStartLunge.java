package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.client.util.LungeChargeInfo;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCStartLunge implements IPacket {

    private final byte duration;
    private final int entityId;
    private final InteractionHand hand;

    public PacketSCStartLunge(int entityId, InteractionHand hand, int duration) {
        this.entityId = entityId;
        this.hand = hand;
        this.duration = (byte) duration;
    }

    public static PacketSCStartLunge decode(FriendlyByteBuf buffer) {
        return new PacketSCStartLunge(buffer.readVarInt(),
                                      buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                                      buffer.readByte());
    }

    public static void encode(PacketSCStartLunge packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeBoolean(packet.hand == InteractionHand.MAIN_HAND);
        buffer.writeByte(packet.duration);
    }

    public static void handle(PacketSCStartLunge packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ItemStack lungeStack = ((LivingEntity) Evolution.PROXY.getClientLevel().getEntity(packet.entityId)).getItemInHand(packet.hand);
                LungeChargeInfo lunge = ClientEvents.ABOUT_TO_LUNGE_PLAYERS.get(packet.entityId);
                if (lunge == null) {
                    ClientEvents.ABOUT_TO_LUNGE_PLAYERS.put(packet.entityId, new LungeChargeInfo(packet.hand, lungeStack, packet.duration));
                }
                else {
                    lunge.addInfo(packet.hand, lungeStack, packet.duration);
                }
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
