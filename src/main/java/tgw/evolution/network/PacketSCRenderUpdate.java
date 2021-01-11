package tgw.evolution.network;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.fluids.FluidGeneric;
import tgw.evolution.blocks.tileentities.TELoggable;
import tgw.evolution.util.BlockFlags;

import java.util.function.Supplier;

public class PacketSCRenderUpdate implements IPacket {

    private final int amount;
    private final Fluid fluid;
    private final BlockPos pos;

    public PacketSCRenderUpdate(BlockPos pos, Fluid fluid, int amount) {
        this.pos = pos;
        this.fluid = fluid == null ? Fluids.EMPTY : fluid;
        this.amount = amount;
    }

    public static PacketSCRenderUpdate decode(PacketBuffer buffer) {
        return new PacketSCRenderUpdate(buffer.readBlockPos(), FluidGeneric.byId(buffer.readInt()), buffer.readInt());
    }

    public static void encode(PacketSCRenderUpdate packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.fluid instanceof FluidGeneric ? ((FluidGeneric) packet.fluid).getId() : 0);
        buffer.writeInt(packet.amount);
    }

    public static void handle(PacketSCRenderUpdate packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                World world = Evolution.PROXY.getClientWorld();
                BlockState state = world.getBlockState(packet.pos);
                TileEntity tile = world.getTileEntity(packet.pos);
                if (!(tile instanceof TELoggable) && packet.fluid instanceof FluidGeneric) {
                    //noinspection VariableNotUsedInsideIf
                    if (tile != null) {
                        world.removeTileEntity(packet.pos);
                    }
                    TELoggable newTile = new TELoggable();
                    newTile.setAmountAndFluid(packet.amount, (FluidGeneric) packet.fluid);
                    world.setTileEntity(packet.pos, newTile);
                }
                world.notifyBlockUpdate(packet.pos, state, state, BlockFlags.RERENDER);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
