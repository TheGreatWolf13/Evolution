package tgw.evolution.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import tgw.evolution.inventory.extendedinventory.ContainerInventoryProvider;

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
        NetworkEvent.Context c = context.get();
        if (IPacket.checkSide(packet, c)) {
            c.enqueueWork(() -> {
                ServerPlayer player = c.getSender();
                assert player != null;
                NetworkHooks.openGui(player, new ContainerInventoryProvider());
            });
            c.setPacketHandled(true);
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.SERVER;
    }
}
