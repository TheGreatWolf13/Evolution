//package tgw.evolution.network;
//
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.world.level.entity.EntityAccess;
//import net.minecraftforge.fml.LogicalSide;
//import net.minecraftforge.network.NetworkEvent;
//import tgw.evolution.events.ClientEvents;
//
//import java.util.function.Supplier;
//
//public class PacketSCCamera implements IPacket {
//
//    private final int entityId;
//
//    public PacketSCCamera(int entityId) {
//        this.entityId = entityId;
//    }
//
//    public PacketSCCamera(EntityAccess entity) {
//        this.entityId = entity.getId();
//    }
//
//    public static PacketSCCamera decode(FriendlyByteBuf buf) {
//        return new PacketSCCamera(buf.readVarInt());
//    }
//
//    public static void encode(PacketSCCamera packet, FriendlyByteBuf buf) {
//        buf.writeVarInt(packet.entityId);
//    }
//
//    public static void handle(PacketSCCamera packet, Supplier<NetworkEvent.Context> context) {
//        if (IPacket.checkSide(packet, context)) {
//            context.get().enqueueWork(() -> ClientEvents.getInstance().setCameraEntity(packet.entityId));
//            context.get().setPacketHandled(true);
//        }
//    }
//
//    @Override
//    public LogicalSide getDestinationSide() {
//        return LogicalSide.CLIENT;
//    }
//}
