package tgw.evolution.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.init.EvolutionNetwork;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class PacketCSUpdateBeltBackItem implements IPacket {

    private final boolean back;
    @Nonnull
    private final ItemStack stack;

    public PacketCSUpdateBeltBackItem(@Nonnull ItemStack stack, boolean back) {
        this.stack = stack;
        this.back = back;
    }

    public static PacketCSUpdateBeltBackItem decode(PacketBuffer buffer) {
        return new PacketCSUpdateBeltBackItem(buffer.readItemStack(), buffer.readBoolean());
    }

    public static void encode(PacketCSUpdateBeltBackItem packet, PacketBuffer buffer) {
        buffer.writeItemStack(packet.stack, false);
        buffer.writeBoolean(packet.back);
    }

    public static void handle(PacketCSUpdateBeltBackItem packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = context.get().getSender();
                EvolutionNetwork.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> player),
                                               new PacketSCUpdateBeltBackItem(player.getEntityId(), packet.back, packet.stack));
            });
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
