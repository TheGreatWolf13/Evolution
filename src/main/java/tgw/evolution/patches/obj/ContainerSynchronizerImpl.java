package tgw.evolution.patches.obj;

import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;

public class ContainerSynchronizerImpl implements ContainerSynchronizer {

    private final ServerPlayer player;

    public ContainerSynchronizerImpl(ServerPlayer player) {
        this.player = player;
    }

    private void broadcastDataValue(AbstractContainerMenu menu, int i, int j) {
        this.player.connection.send(new ClientboundContainerSetDataPacket(menu.containerId, i, j));
    }

    @Override
    public void sendCarriedChange(AbstractContainerMenu menu, ItemStack stack) {
        this.player.connection.send(new ClientboundContainerSetSlotPacket(-1, menu.incrementStateId(), -1, stack));
    }

    @Override
    public void sendDataChange(AbstractContainerMenu menu, int i, int j) {
        this.broadcastDataValue(menu, i, j);
    }

    @Override
    public void sendInitialData(AbstractContainerMenu menu, NonNullList<ItemStack> items, ItemStack stack, int[] is) {
        this.player.connection.send(new ClientboundContainerSetContentPacket(menu.containerId, menu.incrementStateId(), items, stack));
        for (int i = 0; i < is.length; ++i) {
            this.broadcastDataValue(menu, i, is[i]);
        }
    }

    @Override
    public void sendSlotChange(AbstractContainerMenu menu, int i, ItemStack stack) {
        this.player.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), i, stack));
    }
}
