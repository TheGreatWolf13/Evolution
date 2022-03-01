package tgw.evolution.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.KnappingRecipe;
import tgw.evolution.blocks.tileentities.TEKnapping;

import java.util.function.Supplier;

public class PacketCSSetKnappingType implements IPacket {

    private final BlockPos pos;
    private final KnappingRecipe type;

    public PacketCSSetKnappingType(BlockPos pos, KnappingRecipe type) {
        this.pos = pos;
        this.type = type;
    }

    public static PacketCSSetKnappingType decode(FriendlyByteBuf buffer) {
        return new PacketCSSetKnappingType(buffer.readBlockPos(), KnappingRecipe.byId(buffer.readByte()));
    }

    public static void encode(PacketCSSetKnappingType packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeByte(packet.type.getId());
    }

    public static void handle(PacketCSSetKnappingType packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                Level world = context.get().getSender().level;
                BlockEntity tile = world.getBlockEntity(packet.pos);
                if (tile instanceof TEKnapping knapping) {
                    knapping.setType(packet.type);
                    return;
                }
                Evolution.warn("Could not find TEKnapping at {}", packet.pos);
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
