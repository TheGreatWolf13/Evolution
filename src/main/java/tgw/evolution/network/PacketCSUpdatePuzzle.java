package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.TEPuzzle;
import tgw.evolution.init.EvolutionNetwork;

import java.util.function.Supplier;

public class PacketCSUpdatePuzzle extends PacketAbstract {

    private final BlockPos pos;
    private final ResourceLocation attachmentType;
    private final ResourceLocation targetPool;
    private final String finalState;
    private final boolean checkBB;

    public PacketCSUpdatePuzzle(BlockPos pos, ResourceLocation attachmentType, ResourceLocation targetPool, String finalState, boolean checkBB) {
        super(LogicalSide.SERVER);
        this.pos = pos;
        this.attachmentType = attachmentType;
        this.targetPool = targetPool;
        this.finalState = finalState;
        this.checkBB = checkBB;
    }

    public static void encode(PacketCSUpdatePuzzle packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeResourceLocation(packet.attachmentType);
        buffer.writeResourceLocation(packet.targetPool);
        buffer.writeString(packet.finalState);
        buffer.writeBoolean(packet.checkBB);
    }

    public static PacketCSUpdatePuzzle decode(PacketBuffer buffer) {
        return new PacketCSUpdatePuzzle(buffer.readBlockPos(), buffer.readResourceLocation(), buffer.readResourceLocation(), buffer.readString(), buffer.readBoolean());
    }

    public static void handle(PacketCSUpdatePuzzle packet, Supplier<NetworkEvent.Context> context) {
        if (EvolutionNetwork.checkSide(context, packet)) {
            context.get().enqueueWork(() -> {
                TileEntity tile = context.get().getSender().world.getTileEntity(packet.pos);
                if (!(tile instanceof TEPuzzle)) {
                    Evolution.LOGGER.warn("Could not find TEPuzzle at " + packet.pos);
                    return;
                }
                TEPuzzle tePuzzle = (TEPuzzle) tile;
                tePuzzle.setAttachmentType(packet.attachmentType);
                tePuzzle.setTargetPool(packet.targetPool);
                tePuzzle.setFinalState(packet.finalState);
                tePuzzle.setCheckBB(packet.checkBB);
            });
            context.get().setPacketHandled(true);
        }
    }
}
