package tgw.evolution.patches.obj;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerListenerImpl implements ContainerListener {

    private final ServerPlayer player;

    public ContainerListenerImpl(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void dataChanged(AbstractContainerMenu menu, int i, int j) {
    }

    @Override
    public void slotChanged(AbstractContainerMenu menu, int i, ItemStack stack) {
        Slot slot = menu.getSlot(i);
        if (!(slot instanceof ResultSlot)) {
            if (slot.container == this.player.getInventory()) {
                CriteriaTriggers.INVENTORY_CHANGED.trigger(this.player, this.player.getInventory(), stack);
            }
        }
    }
}
