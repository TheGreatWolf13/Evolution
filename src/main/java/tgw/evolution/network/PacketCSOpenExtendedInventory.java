package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import tgw.evolution.inventory.extendedinventory.ContainerPlayerInventoryProvider;

import java.util.function.Supplier;

public class PacketCSOpenExtendedInventory implements IPacket {

    @SuppressWarnings("unused")
    public static PacketCSOpenExtendedInventory decode(FriendlyByteBuf buffer) {
        return new PacketCSOpenExtendedInventory();
    }

    @SuppressWarnings("unused")
    public static void encode(PacketCSOpenExtendedInventory message, FriendlyByteBuf buffer) {
    }

    public static void handle(PacketCSOpenExtendedInventory packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> NetworkHooks.openGui(context.get().getSender(), new ContainerPlayerInventoryProvider()));
            context.get().setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
