package tgw.evolution.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.init.EvolutionNetwork;

import java.util.function.Supplier;

public class PacketCSStartLunge implements IPacket {

    private final byte duration;
    private final Hand hand;

    public PacketCSStartLunge(Hand hand, int duration) {
        this.hand = hand;
        this.duration = (byte) duration;
    }

    public static PacketCSStartLunge decode(PacketBuffer buffer) {
        return new PacketCSStartLunge(buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND, buffer.readByte());
    }

    public static void encode(PacketCSStartLunge packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.hand == Hand.MAIN_HAND);
        buffer.writeByte(packet.duration);
    }

    public static void handle(PacketCSStartLunge packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = context.get().getSender();
                EvolutionNetwork.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> player),
                                               new PacketSCStartLunge(player.getId(), packet.hand, packet.duration));
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
