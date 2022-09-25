package tgw.evolution.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                ServerPlayer player = c.getSender();
                assert player != null;
                BlockEntity tile = player.level.getBlockEntity(packet.pos);
                if (tile instanceof TEKnapping knapping) {
                    knapping.setType(packet.type);
                    return;
                }
                Evolution.warn("Could not find TEKnapping at {}", packet.pos);
            });
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
