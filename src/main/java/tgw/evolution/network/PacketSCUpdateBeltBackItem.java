package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import tgw.evolution.events.ClientEvents;

import java.util.function.Supplier;

public class PacketSCUpdateBeltBackItem implements IPacket {

    private final boolean back;
    private final int entityId;
    private final ItemStack stack;

    public PacketSCUpdateBeltBackItem(int entityId, boolean back, ItemStack stack) {
        this.stack = stack;
        this.back = back;
        this.entityId = entityId;
    }

    public static PacketSCUpdateBeltBackItem decode(FriendlyByteBuf buffer) {
        return new PacketSCUpdateBeltBackItem(buffer.readInt(), buffer.readBoolean(), buffer.readItem());
    }

    public static void encode(PacketSCUpdateBeltBackItem packet, FriendlyByteBuf buffer) {
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
