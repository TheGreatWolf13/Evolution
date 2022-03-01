package tgw.evolution.client.gui;

import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public interface IGuiScreenHandler {

    void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed);

    boolean disableRMBDraggingFunctionality();

    Slot getSlotUnderMouse(double mouseX, double mouseY);

    List<Slot> getSlots();

    boolean isCraftingOutput(Slot slot);

    boolean isIgnored(Slot slot);
}
