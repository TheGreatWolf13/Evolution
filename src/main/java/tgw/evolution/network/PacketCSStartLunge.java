package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import tgw.evolution.init.EvolutionNetwork;

import java.util.function.Supplier;

public class PacketCSStartLunge implements IPacket {

    private final byte duration;
    private final InteractionHand hand;

    public PacketCSStartLunge(InteractionHand hand, int duration) {
        this.hand = hand;
        this.duration = (byte) duration;
    }

    public static PacketCSStartLunge decode(FriendlyByteBuf buffer) {
        return new PacketCSStartLunge(buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, buffer.readByte());
    }

    public static void encode(PacketCSStartLunge packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.hand == InteractionHand.MAIN_HAND);
        buffer.writeByte(packet.duration);
    }

    public static void handle(PacketCSStartLunge packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
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
