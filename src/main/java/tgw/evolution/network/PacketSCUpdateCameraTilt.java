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
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> Evolution.PROXY.getClientPlayer().hurtDir = packet.attackedAtYaw);
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
