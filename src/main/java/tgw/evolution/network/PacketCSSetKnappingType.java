package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.EnumKnapping;
import tgw.evolution.blocks.tileentities.TEKnapping;

import java.util.function.Supplier;

public class PacketCSSetKnappingType implements IPacket {

    private final BlockPos pos;
    private final EnumKnapping type;

    public PacketCSSetKnappingType(BlockPos pos, EnumKnapping type) {
        this.pos = pos;
        this.type = type;
    }

    public static PacketCSSetKnappingType decode(PacketBuffer buffer) {
        return new PacketCSSetKnappingType(buffer.readBlockPos(), EnumKnapping.byId(buffer.readByte()));
    }

    public static void encode(PacketCSSetKnappingType packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeByte(packet.type.getId());
    }

    public static void handle(PacketCSSetKnappingType packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                World world = context.get().getSender().world;
                TileEntity tile = world.getTileEntity(packet.pos);
                if (tile instanceof TEKnapping) {
                    ((TEKnapping) tile).setType(packet.type);
                    return;
                }
                Evolution.LOGGER.warn("Could not find TEKnapping at {}", packet.pos);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
