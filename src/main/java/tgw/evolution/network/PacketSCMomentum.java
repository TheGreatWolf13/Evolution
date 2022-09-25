package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.util.math.Vec3d;

import java.util.function.Supplier;

public class PacketSCMomentum implements IPacket {

    private final float x;
    private final float y;
    private final float z;

    public PacketSCMomentum(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static PacketSCMomentum decode(FriendlyByteBuf buf) {
        return new PacketSCMomentum(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static void encode(PacketSCMomentum packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.x);
        buf.writeFloat(packet.y);
        buf.writeFloat(packet.z);
    }

    public static void handle(PacketSCMomentum packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> ((Vec3d) Evolution.PROXY.getClientPlayer().getDeltaMovement()).addMutable(packet.x, packet.y, packet.z));
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
