package tgw.evolution.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.TEPuzzle;

import java.util.function.Supplier;

public class PacketCSUpdatePuzzle implements IPacket {

    private final ResourceLocation attachmentType;
    private final boolean checkBB;
    private final String finalState;
    private final BlockPos pos;
    private final ResourceLocation targetPool;

    public PacketCSUpdatePuzzle(BlockPos pos, ResourceLocation attachmentType, ResourceLocation targetPool, String finalState, boolean checkBB) {
        this.pos = pos;
        this.attachmentType = attachmentType;
        this.targetPool = targetPool;
        this.finalState = finalState;
        this.checkBB = checkBB;
    }

    public static PacketCSUpdatePuzzle decode(FriendlyByteBuf buffer) {
        return new PacketCSUpdatePuzzle(buffer.readBlockPos(),
                                        buffer.readResourceLocation(),
                                        buffer.readResourceLocation(),
                                        buffer.readUtf(),
                                        buffer.readBoolean());
    }

    public static void encode(PacketCSUpdatePuzzle packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeResourceLocation(packet.attachmentType);
        buffer.writeResourceLocation(packet.targetPool);
        buffer.writeUtf(packet.finalState);
        buffer.writeBoolean(packet.checkBB);
    }

    public static void handle(PacketCSUpdatePuzzle packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                ServerPlayer player = c.getSender();
                assert player != null;
                BlockEntity tile = player.level.getBlockEntity(packet.pos);
                if (!(tile instanceof TEPuzzle puzzle)) {
                    Evolution.warn("Could not find TEPuzzle at " + packet.pos);
                    return;
                }
                puzzle.setAttachmentType(packet.attachmentType);
                puzzle.setTargetPool(packet.targetPool);
                puzzle.setFinalState(packet.finalState);
                puzzle.setCheckBB(packet.checkBB);
            });
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
