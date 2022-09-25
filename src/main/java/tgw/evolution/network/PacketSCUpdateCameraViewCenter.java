package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.patches.IClientChunkCachePatch;

import java.util.function.Supplier;

public class PacketSCUpdateCameraViewCenter implements IPacket {

    private final int camX;
    private final int camZ;

    public PacketSCUpdateCameraViewCenter(int camX, int camZ) {
        this.camX = camX;
        this.camZ = camZ;
    }

    public static PacketSCUpdateCameraViewCenter decode(FriendlyByteBuf buf) {
        return new PacketSCUpdateCameraViewCenter(buf.readVarInt(), buf.readVarInt());
    }

    public static void encode(PacketSCUpdateCameraViewCenter packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.camX);
        buf.writeVarInt(packet.camZ);
    }

    public static void handle(PacketSCUpdateCameraViewCenter packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> ((IClientChunkCachePatch) Evolution.PROXY.getClientLevel().getChunkSource()).updateCameraViewCenter(packet.camX,
                                                                                                                                    packet.camZ));
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
