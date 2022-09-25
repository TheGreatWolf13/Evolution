package tgw.evolution.client.gui;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;

public class GuiContainerCreativeHandler extends GuiContainerHandler {

    public GuiContainerCreativeHandler(CreativeModeInventoryScreen guiContainerCreative) {
        super(guiContainerCreative);
    }

    @Override
    public boolean isIgnored(Slot slot) {
        if (super.isIgnored(slot)) {
            return true;
        }
        assert this.mc.player != null;
        return slot.container != this.mc.player.getInventory();
    }
}
