package tgw.evolution.network;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.client.util.LungeChargeInfo;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCStartLunge implements IPacket {

    private final byte duration;
    private final int entityId;
    private final Hand hand;

    public PacketSCStartLunge(int entityId, Hand hand, int duration) {
        this.entityId = entityId;
        this.hand = hand;
        this.duration = (byte) duration;
    }

    public static PacketSCStartLunge decode(PacketBuffer buffer) {
        return new PacketSCStartLunge(buffer.readVarInt(), buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND, buffer.readByte());
    }

    public static void encode(PacketSCStartLunge packet, PacketBuffer buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeBoolean(packet.hand == Hand.MAIN_HAND);
        buffer.writeByte(packet.duration);
    }

    public static void handle(PacketSCStartLunge packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ItemStack lungeStack = ((LivingEntity) Evolution.PROXY.getClientWorld().getEntity(packet.entityId)).getItemInHand(packet.hand);
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
