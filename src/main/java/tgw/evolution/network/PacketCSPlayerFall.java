package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.init.EvolutionNetwork;

import java.util.function.Supplier;

public class PacketCSPlayerFall extends PacketAbstract {

    private final double velocity;
    private final double distanceOfSlowDown;

    public PacketCSPlayerFall(double velocity, double distanceOfSlowDown) {
        super(LogicalSide.SERVER);
        this.velocity = velocity;
        this.distanceOfSlowDown = distanceOfSlowDown;
    }

    public static void encode(PacketCSPlayerFall packet, PacketBuffer buffer) {
        buffer.writeDouble(packet.velocity);
        buffer.writeDouble(packet.distanceOfSlowDown);
    }

    public static PacketCSPlayerFall decode(PacketBuffer buffer) {
        return new PacketCSPlayerFall(buffer.readDouble(), buffer.readDouble());
    }

    public static void handle(PacketCSPlayerFall packet, Supplier<NetworkEvent.Context> context) {
        if (EvolutionNetwork.checkSide(context, packet)) {
            context.get().enqueueWork(() -> EntityEvents.calculateFallDamage(context.get().getSender(), packet.velocity, packet.distanceOfSlowDown));
            context.get().setPacketHandled(true);
        }
    }
}
