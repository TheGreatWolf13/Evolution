package tgw.evolution.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.inventory.ContainerPlayerInventoryProvider;

import java.util.function.Supplier;

public class PacketCSOpenExtendedInventory extends PacketAbstract {

    public PacketCSOpenExtendedInventory() {
        super(LogicalSide.SERVER);
    }

    @SuppressWarnings("unused")
    public static void encode(PacketCSOpenExtendedInventory message, PacketBuffer buffer) {
    }

    @SuppressWarnings("unused")
    public static PacketCSOpenExtendedInventory decode(PacketBuffer buffer) {
        return new PacketCSOpenExtendedInventory();
    }

    public static void handle(PacketCSOpenExtendedInventory packet, Supplier<NetworkEvent.Context> context) {
        if (EvolutionNetwork.checkSide(context, packet)) {
            context.get().enqueueWork(() -> NetworkHooks.openGui(context.get().getSender(), new ContainerPlayerInventoryProvider()));
            context.get().setPacketHandled(true);
        }
    }
}
