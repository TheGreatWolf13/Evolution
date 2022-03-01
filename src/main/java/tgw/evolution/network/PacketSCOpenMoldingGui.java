package tgw.evolution.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.client.gui.ScreenMolding;

import java.util.function.Supplier;

public class PacketSCOpenMoldingGui implements IPacket {

    private final BlockPos pos;

    public PacketSCOpenMoldingGui(BlockPos pos) {
        this.pos = pos;
    }

    public static PacketSCOpenMoldingGui decode(FriendlyByteBuf buffer) {
        return new PacketSCOpenMoldingGui(buffer.readBlockPos());
    }

    public static void encode(PacketSCOpenMoldingGui packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static void handle(PacketSCOpenMoldingGui packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> ScreenMolding.open(packet.pos));
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
