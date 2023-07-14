//package tgw.evolution.network;
//
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraftforge.fml.LogicalSide;
//import net.minecraftforge.network.NetworkEvent;
//import tgw.evolution.Evolution;
//
//import java.util.function.Supplier;
//
//public class PacketSCSyncServerConfig implements IPacket {
//
//    private final byte[] data;
//    private final String filename;
//
//    public PacketSCSyncServerConfig(String filename, byte[] data) {
//        this.filename = filename;
//        this.data = data;
//    }
//
//    public static PacketSCSyncServerConfig decode(FriendlyByteBuf buf) {
//        return new PacketSCSyncServerConfig(buf.readUtf(), buf.readByteArray());
//    }
//
//    public static void encode(PacketSCSyncServerConfig packet, FriendlyByteBuf buf) {
//        buf.writeUtf(packet.filename);
//        buf.writeByteArray(packet.data);
//    }
//
//    public static void handle(PacketSCSyncServerConfig packet, Supplier<NetworkEvent.Context> context) {
//        NetworkEvent.Context c = context.get();
//        if (IPacket.checkSide(packet, c)) {
//            c.enqueueWork(() -> Evolution.PACKET_HANDLER.handleSyncServerConfig(packet.filename, packet.data));
//            c.setPacketHandled(true);
//        }
//    }
//
//    @Override
//    public LogicalSide getDestinationSide() {
//        return LogicalSide.CLIENT;
//    }
//}
