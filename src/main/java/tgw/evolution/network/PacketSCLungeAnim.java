package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCLungeAnim implements IPacket {

    private final int entityId;
    private final InteractionHand hand;

    public PacketSCLungeAnim(int entityId, InteractionHand hand) {
        this.entityId = entityId;
        this.hand = hand;
    }

    public static PacketSCLungeAnim decode(FriendlyByteBuf buffer) {
        return new PacketSCLungeAnim(buffer.readVarInt(), buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public static void encode(PacketSCLungeAnim packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeBoolean(packet.hand == InteractionHand.MAIN_HAND);
    }

    public static void handle(PacketSCLungeAnim packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> ClientEvents.addLungingPlayer(packet.entityId, packet.hand));
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
