package tgw.evolution.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.init.EvolutionNetwork;

import java.util.function.Supplier;

public class PacketCSLungeAnim implements IPacket {

    private final Hand hand;

    public PacketCSLungeAnim(Hand hand) {
        this.hand = hand;
    }

    public static PacketCSLungeAnim decode(PacketBuffer buffer) {
        return new PacketCSLungeAnim(buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND);
    }

    public static void encode(PacketCSLungeAnim packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.hand == Hand.MAIN_HAND);
    }

    public static void handle(PacketCSLungeAnim packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = context.get().getSender();
                EvolutionNetwork.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> player),
                                               new PacketSCLungeAnim(player.getId(), packet.hand));
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
