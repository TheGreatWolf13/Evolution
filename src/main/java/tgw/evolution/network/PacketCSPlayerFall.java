package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.hooks.LivingHooks;

import java.util.function.Supplier;

public class PacketCSPlayerFall implements IPacket {

    private final double distanceOfSlowDown;
    private final double velocity;
    private final boolean water;

    public PacketCSPlayerFall(double velocity, double distanceOfSlowDown, boolean water) {
        this.velocity = velocity;
        this.distanceOfSlowDown = distanceOfSlowDown;
        this.water = water;
    }

    public static PacketCSPlayerFall decode(FriendlyByteBuf buffer) {
        return new PacketCSPlayerFall(buffer.readDouble(), buffer.readDouble(), buffer.readBoolean());
    }

    public static void encode(PacketCSPlayerFall packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.velocity);
        buffer.writeDouble(packet.distanceOfSlowDown);
        buffer.writeBoolean(packet.water);
    }

    public static void handle(PacketCSPlayerFall packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                ServerPlayer player = c.getSender();
                assert player != null;
                LivingHooks.calculateFallDamage(player, packet.velocity, packet.distanceOfSlowDown, packet.water);
            });
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
