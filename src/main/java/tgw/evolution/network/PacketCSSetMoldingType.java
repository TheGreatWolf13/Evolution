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
import tgw.evolution.init.EvolutionNetwork;

import java.util.function.Supplier;

public class PacketCSSetMoldingType extends PacketAbstract {

    private final BlockPos pos;
    private final EnumMolding molding;

    public PacketCSSetMoldingType(BlockPos pos, EnumMolding molding) {
        super(LogicalSide.SERVER);
        this.pos = pos;
        this.molding = molding;
    }

    public static void encode(PacketCSSetMoldingType packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeByte(packet.molding.getId());
    }

    public static PacketCSSetMoldingType decode(PacketBuffer buffer) {
        return new PacketCSSetMoldingType(buffer.readBlockPos(), EnumMolding.byId(buffer.readByte()));
    }

    public static void handle(PacketCSSetMoldingType packet, Supplier<NetworkEvent.Context> context) {
        if (EvolutionNetwork.checkSide(context, packet)) {
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
}
