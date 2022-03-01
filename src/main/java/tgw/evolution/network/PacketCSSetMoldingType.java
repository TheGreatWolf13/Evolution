package tgw.evolution.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.EnumMolding;
import tgw.evolution.blocks.tileentities.TEMolding;

import java.util.function.Supplier;

public class PacketCSSetMoldingType implements IPacket {

    private final EnumMolding molding;
    private final BlockPos pos;

    public PacketCSSetMoldingType(BlockPos pos, EnumMolding molding) {
        this.pos = pos;
        this.molding = molding;
    }

    public static PacketCSSetMoldingType decode(FriendlyByteBuf buffer) {
        return new PacketCSSetMoldingType(buffer.readBlockPos(), EnumMolding.byId(buffer.readByte()));
    }

    public static void encode(PacketCSSetMoldingType packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeByte(packet.molding.getId());
    }

    public static void handle(PacketCSSetMoldingType packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                Level level = context.get().getSender().level;
                BlockEntity tile = level.getBlockEntity(packet.pos);
                if (tile instanceof TEMolding molding) {
                    molding.setType(packet.molding);
                    return;
                }
                Evolution.warn("Could not find TEMolding at {}", packet.pos);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
