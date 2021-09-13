package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCHandAnimation implements IPacket {

    private final Hand hand;

    public PacketSCHandAnimation(Hand hand) {
        this.hand = hand;
    }

    public static PacketSCHandAnimation decode(PacketBuffer buffer) {
        Hand hand = buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
        return new PacketSCHandAnimation(hand);
    }

    public static void encode(PacketSCHandAnimation message, PacketBuffer buffer) {
        buffer.writeBoolean(message.hand == Hand.MAIN_HAND);
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
