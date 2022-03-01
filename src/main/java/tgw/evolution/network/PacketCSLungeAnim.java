package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import tgw.evolution.init.EvolutionNetwork;

import java.util.function.Supplier;

public class PacketCSLungeAnim implements IPacket {

    private final InteractionHand hand;

    public PacketCSLungeAnim(InteractionHand hand) {
        this.hand = hand;
    }

    public static PacketCSLungeAnim decode(FriendlyByteBuf buffer) {
        return new PacketCSLungeAnim(buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public static void encode(PacketCSLungeAnim packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.hand == InteractionHand.MAIN_HAND);
    }

    public static void handle(PacketCSLungeAnim packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
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
