package tgw.evolution.client.gui;

import net.minecraft.inventory.container.Slot;

import java.util.List;

public interface IGuiScreenHandler {

    void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed);

    boolean disableRMBDraggingFunctionality();

    Slot getSlotUnderMouse(double mouseX, double mouseY);

    List<Slot> getSlots();

    boolean isCraftingOutput(Slot slot);

    boolean isIgnored(Slot slot);

    boolean isMouseTweaksDisabled();

    boolean isWheelTweakDisabled();
}
