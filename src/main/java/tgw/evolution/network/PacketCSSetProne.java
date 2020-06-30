package tgw.evolution.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionNetwork;

import java.util.function.Supplier;

public class PacketCSSetProne extends PacketAbstract {

    private final boolean proned;

    public PacketCSSetProne(boolean proned) {
        super(LogicalSide.SERVER);
        this.proned = proned;
    }

    public static void encode(PacketCSSetProne packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.proned);
    }

    public static PacketCSSetProne decode(PacketBuffer buffer) {
        return new PacketCSSetProne(buffer.readBoolean());
    }

    public static void handle(PacketCSSetProne packet, Supplier<NetworkEvent.Context> context) {
        if (EvolutionNetwork.checkSide(context, packet)) {
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = context.get().getSender();
                Evolution.PRONED_PLAYERS.put(player.getUniqueID(), packet.proned);
            });
            context.get().setPacketHandled(true);
        }
    }
}
