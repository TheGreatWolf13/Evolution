package tgw.evolution.client.gui;

import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.util.MouseButton;

import java.util.List;

public interface IGuiScreenHandler {

    void clickSlot(Slot slot, @MouseButton int mouseButton, boolean shiftPressed);

    @Nullable Slot getSlotUnderMouse(double mouseX, double mouseY);

    List<Slot> getSlots();

    boolean isCraftingOutput(Slot slot);

    boolean isIgnored(Slot slot);
}
