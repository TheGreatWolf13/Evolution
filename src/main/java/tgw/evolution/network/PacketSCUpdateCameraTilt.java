package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;

import java.util.function.Supplier;

public class PacketSCUpdateCameraTilt implements IPacket {

    private final float attackedAtYaw;

    private PacketSCUpdateCameraTilt(float attackedAtYaw) {
        this.attackedAtYaw = attackedAtYaw;
    }

    public PacketSCUpdateCameraTilt(Player player) {
        this.attackedAtYaw = player.hurtDir;
    }

    public static PacketSCUpdateCameraTilt decode(FriendlyByteBuf buffer) {
        return new PacketSCUpdateCameraTilt(buffer.readFloat());
    }

    public static void encode(PacketSCUpdateCameraTilt packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.attackedAtYaw);
    }

    public static void handle(PacketSCUpdateCameraTilt packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> Evolution.PROXY.getClientPlayer().hurtDir = packet.attackedAtYaw);
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
