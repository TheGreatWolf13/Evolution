package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;

import java.util.function.Supplier;

public class PacketSCMovement implements IPacket {

    private final double motionX;
    private final double motionY;
    private final double motionZ;

    public PacketSCMovement(double motionX, double motionY, double motionZ) {
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }

    public static PacketSCMovement decode(FriendlyByteBuf buffer) {
        return new PacketSCMovement(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void encode(PacketSCMovement packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.motionX);
        buffer.writeDouble(packet.motionY);
        buffer.writeDouble(packet.motionZ);
    }

    public static void handle(PacketSCMovement packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> Evolution.PROXY.getClientPlayer().setDeltaMovement(packet.motionX, packet.motionY, packet.motionZ));
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
