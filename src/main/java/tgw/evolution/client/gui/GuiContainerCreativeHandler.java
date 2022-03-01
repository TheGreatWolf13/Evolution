package tgw.evolution.client.gui;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiContainerCreativeHandler extends GuiContainerHandler {

    public GuiContainerCreativeHandler(CreativeModeInventoryScreen guiContainerCreative) {
        super(guiContainerCreative);
    }

    @Override
    public boolean isIgnored(Slot slot) {
        return super.isIgnored(slot) || slot.container != this.mc.player.getInventory();
    }
}
