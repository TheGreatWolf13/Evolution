package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.client.gui.ScreenKnapping;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.util.EnumRockVariant;

import java.util.function.Supplier;

public class PacketSCOpenKnappingGui extends PacketAbstract {

    private final EnumRockVariant variant;
    private final BlockPos pos;

    public PacketSCOpenKnappingGui(BlockPos pos, EnumRockVariant variant) {
        super(LogicalSide.CLIENT);
        this.pos = pos;
        this.variant = variant;
    }

    public static void encode(PacketSCOpenKnappingGui packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeByte(packet.variant.getId());
    }

    public static PacketSCOpenKnappingGui decode(PacketBuffer buffer) {
        return new PacketSCOpenKnappingGui(buffer.readBlockPos(), EnumRockVariant.fromId(buffer.readByte()));
    }

    public static void handle(PacketSCOpenKnappingGui packet, Supplier<NetworkEvent.Context> context) {
        if (EvolutionNetwork.checkSide(context, packet)) {
            context.get().enqueueWork(() -> ScreenKnapping.open(packet.pos, packet.variant));
            context.get().setPacketHandled(true);
        }
    }
}
