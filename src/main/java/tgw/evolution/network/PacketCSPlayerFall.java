package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.events.EntityEvents;

import java.util.function.Supplier;

public class PacketCSPlayerFall implements IPacket {

    private final double distanceOfSlowDown;
    private final double velocity;

    public PacketCSPlayerFall(double velocity, double distanceOfSlowDown) {
        this.velocity = velocity;
        this.distanceOfSlowDown = distanceOfSlowDown;
    }

    public static PacketCSPlayerFall decode(FriendlyByteBuf buffer) {
        return new PacketCSPlayerFall(buffer.readDouble(), buffer.readDouble());
    }

    public static void encode(PacketCSPlayerFall packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.velocity);
        buffer.writeDouble(packet.distanceOfSlowDown);
    }

    public static void handle(PacketCSPlayerFall packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get()
                   .enqueueWork(() -> EntityEvents.calculateFallDamage(context.get().getSender(), packet.velocity, packet.distanceOfSlowDown, false));
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
