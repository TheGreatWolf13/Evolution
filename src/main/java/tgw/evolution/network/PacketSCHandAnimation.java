package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCHandAnimation implements IPacket {

    private final InteractionHand hand;

    public PacketSCHandAnimation(InteractionHand hand) {
        this.hand = hand;
    }

    public static PacketSCHandAnimation decode(FriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        return new PacketSCHandAnimation(hand);
    }

    public static void encode(PacketSCHandAnimation message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.hand == InteractionHand.MAIN_HAND);
    }

    public static void handle(PacketSCHandAnimation packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> ClientEvents.getInstance().swingArm(packet.hand));
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
