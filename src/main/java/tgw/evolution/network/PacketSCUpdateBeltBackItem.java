package tgw.evolution.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.events.ClientEvents;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class PacketSCUpdateBeltBackItem implements IPacket {

    private final boolean back;
    private final int entityId;
    @Nonnull
    private final ItemStack stack;

    public PacketSCUpdateBeltBackItem(int entityId, boolean back, @Nonnull ItemStack stack) {
        this.stack = stack;
        this.back = back;
        this.entityId = entityId;
    }

    public static PacketSCUpdateBeltBackItem decode(PacketBuffer buffer) {
        return new PacketSCUpdateBeltBackItem(buffer.readInt(), buffer.readBoolean(), buffer.readItemStack());
    }

    public static void encode(PacketSCUpdateBeltBackItem packet, PacketBuffer buffer) {
        buffer.writeInt(packet.entityId);
        buffer.writeBoolean(packet.back);
        buffer.writeItemStack(packet.stack, false);
    }

    public static void handle(PacketSCUpdateBeltBackItem packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> {
                if (packet.back) {
                    ClientEvents.BACK_ITEMS.put(packet.entityId, packet.stack);
                }
                else {
                    ClientEvents.BELT_ITEMS.put(packet.entityId, packet.stack);
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
