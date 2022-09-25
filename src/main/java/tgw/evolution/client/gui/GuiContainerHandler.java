package tgw.evolution.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.*;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.util.MouseButton;

import java.util.List;

public class GuiContainerHandler implements IGuiScreenHandler {
    protected final Minecraft mc;
    private final AbstractContainerScreen<?> containerScreen;

    public GuiContainerHandler(AbstractContainerScreen<?> containerScreen) {
        this.mc = Minecraft.getInstance();
        this.containerScreen = containerScreen;
    }

    @Override
    public void clickSlot(Slot slot, @MouseButton int mouseButton, boolean shiftPressed) {
        this.containerScreen.slotClicked(slot, slot.index, mouseButton, shiftPressed ? ClickType.QUICK_MOVE : ClickType.PICKUP);
    }

    @Override
    public @Nullable Slot getSlotUnderMouse(double mouseX, double mouseY) {
        return this.containerScreen.findSlot(mouseX, mouseY);
    }

    @Override
    public List<Slot> getSlots() {
        return this.containerScreen.getMenu().slots;
    }

    @Override
    public boolean isCraftingOutput(Slot slot) {
        return slot instanceof ResultSlot ||
               slot instanceof FurnaceResultSlot ||
               slot instanceof MerchantResultSlot ||
               this.containerScreen.getMenu() instanceof ItemCombinerMenu && slot.index == 2;
    }

    @Override
    public boolean isIgnored(Slot slot) {
        return false;
    }
}
