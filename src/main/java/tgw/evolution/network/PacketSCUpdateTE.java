package tgw.evolution.network;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.Evolution;

import java.util.function.Supplier;

public class PacketSCUpdateTE implements IPacket {

    private final CompoundNBT nbt;
    private final BlockPos pos;

    public PacketSCUpdateTE(TileEntity tile) {
        this.pos = tile.getPos();
        this.nbt = tile.getUpdateTag();
    }

    private PacketSCUpdateTE(BlockPos pos, CompoundNBT nbt) {
        this.pos = pos;
        this.nbt = nbt;
    }

    public static PacketSCUpdateTE decode(PacketBuffer buffer) {
        return new PacketSCUpdateTE(buffer.readBlockPos(), buffer.readCompoundTag());
    }

    public static void encode(PacketSCUpdateTE packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeCompoundTag(packet.nbt);
    }

    public static void handle(PacketSCUpdateTE packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                World world = Evolution.PROXY.getClientWorld();
                TileEntity tile = world.getTileEntity(packet.pos);
                if (tile == null) {
                    Evolution.LOGGER.error("Error while updating tile entity at {}: null", packet.pos);
                }
                else {
                    tile.handleUpdateTag(packet.nbt);
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
