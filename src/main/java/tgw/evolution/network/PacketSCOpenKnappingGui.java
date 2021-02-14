package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.client.gui.ScreenKnapping;
import tgw.evolution.util.RockVariant;

import java.util.function.Supplier;

public class PacketSCOpenKnappingGui implements IPacket {

    private final BlockPos pos;
    private final RockVariant variant;

    public PacketSCOpenKnappingGui(BlockPos pos, RockVariant variant) {
        this.pos = pos;
        this.variant = variant;
    }

    public static PacketSCOpenKnappingGui decode(PacketBuffer buffer) {
        return new PacketSCOpenKnappingGui(buffer.readBlockPos(), RockVariant.fromId(buffer.readByte()));
    }

    public static void encode(PacketSCOpenKnappingGui packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeByte(packet.variant.getId());
    }

    public static void handle(PacketSCOpenKnappingGui packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> ScreenKnapping.open(packet.pos, packet.variant));
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
