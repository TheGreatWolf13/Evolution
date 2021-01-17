package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.client.LungeAttackInfo;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCLungeAnim implements IPacket {

    private final int entityId;
    private final Hand hand;

    public PacketSCLungeAnim(int entityId, Hand hand) {
        this.entityId = entityId;
        this.hand = hand;
    }

    public static PacketSCLungeAnim decode(PacketBuffer buffer) {
        return new PacketSCLungeAnim(buffer.readInt(), buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND);
    }

    public static void encode(PacketSCLungeAnim packet, PacketBuffer buffer) {
        buffer.writeInt(packet.entityId);
        buffer.writeBoolean(packet.hand == Hand.MAIN_HAND);
    }

    public static void handle(PacketSCLungeAnim packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                LungeAttackInfo lunge = ClientEvents.LUNGING_PLAYERS.get(packet.entityId);
                if (lunge == null) {
                    ClientEvents.LUNGING_PLAYERS.put(packet.entityId, new LungeAttackInfo(packet.hand));
                }
                else {
                    lunge.addInfo(packet.hand);
                }
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
