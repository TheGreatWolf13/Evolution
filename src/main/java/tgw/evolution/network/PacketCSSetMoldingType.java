package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.EnumMolding;
import tgw.evolution.blocks.tileentities.TEMolding;

import java.util.function.Supplier;

public class PacketCSSetMoldingType implements IPacket {

    private final BlockPos pos;
    private final EnumMolding molding;

    public PacketCSSetMoldingType(BlockPos pos, EnumMolding molding) {
        this.pos = pos;
        this.molding = molding;
    }

    public static PacketCSSetMoldingType decode(PacketBuffer buffer) {
        return new PacketCSSetMoldingType(buffer.readBlockPos(), EnumMolding.byId(buffer.readByte()));
    }

    public static void encode(PacketCSSetMoldingType packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeByte(packet.molding.getId());
    }

    public static void handle(PacketCSSetMoldingType packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                World world = context.get().getSender().world;
                TileEntity tile = world.getTileEntity(packet.pos);
                if (tile instanceof TEMolding) {
                    ((TEMolding) tile).setType(packet.molding);
                    return;
                }
                Evolution.LOGGER.warn("Could not find TEMolding at {}", packet.pos);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
