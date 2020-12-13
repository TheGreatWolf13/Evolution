package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCRemoveEffect implements IPacket {

    private final Effect effect;

    public PacketSCRemoveEffect(Effect effect) {
        this.effect = effect;
    }

    public static PacketSCRemoveEffect decode(PacketBuffer buffer) {
        Effect effect = Effect.get(buffer.readInt());
        return new PacketSCRemoveEffect(effect);
    }

    public static void encode(PacketSCRemoveEffect packet, PacketBuffer buffer) {
        buffer.writeInt(Effect.getId(packet.effect));
    }

    public static void handle(PacketSCRemoveEffect packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> ClientEvents.removePotionEffect(packet.effect));
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
