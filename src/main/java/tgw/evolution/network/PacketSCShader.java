package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCShader implements IPacket {

    public static final int TOGGLE = -1;
    public static final int QUERY = -2;
    private final int shaderId;

    public PacketSCShader(int shaderId) {
        this.shaderId = shaderId;
    }

    public static PacketSCShader decode(PacketBuffer buffer) {
        return new PacketSCShader(buffer.readVarInt());
    }

    public static void encode(PacketSCShader packet, PacketBuffer buffer) {
        buffer.writeVarInt(packet.shaderId);
    }

    public static void handle(PacketSCShader packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> ClientEvents.getInstance().handleShaderPacket(packet.shaderId));
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
