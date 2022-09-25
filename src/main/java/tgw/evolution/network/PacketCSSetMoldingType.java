package tgw.evolution.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                ServerPlayer player = c.getSender();
                assert player != null;
                BlockEntity tile = player.level.getBlockEntity(packet.pos);
                if (tile instanceof TEMolding molding) {
                    molding.setType(packet.molding);
                    return;
                }
                Evolution.warn("Could not find TEMolding at {}", packet.pos);
            });
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
