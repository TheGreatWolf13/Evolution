package tgw.evolution.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.*;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.util.reflection.MethodHandler;

import java.util.List;

public class GuiContainerHandler implements IGuiScreenHandler {
    @SuppressWarnings("rawtypes")
    private static final MethodHandler<ContainerScreen, Void> HANDLE_MOUSE_CLICK = new MethodHandler<>(ContainerScreen.class,
                                                                                                       "func_184098_a",
                                                                                                       Slot.class,
                                                                                                       int.class,
                                                                                                       int.class,
                                                                                                       ClickType.class);
    @SuppressWarnings("rawtypes")
    private static final FieldHandler<ContainerScreen, Boolean> IGNORE_MOUSE_UP = new FieldHandler<>(ContainerScreen.class, "field_146995_H");
    @SuppressWarnings("rawtypes")
    private static final FieldHandler<ContainerScreen, Boolean> DRAG_SPLITTING = new FieldHandler<>(ContainerScreen.class, "field_147007_t");
    @SuppressWarnings("rawtypes")
    private static final FieldHandler<ContainerScreen, Integer> DRAG_SPLITTING_BUTTON = new FieldHandler<>(ContainerScreen.class, "field_146988_G");
    @SuppressWarnings("rawtypes")
    private static final MethodHandler<ContainerScreen, Slot> GET_SELECTED_SLOT = new MethodHandler<>(ContainerScreen.class,
                                                                                                      "func_195360_a",
                                                                                                      double.class,
                                                                                                      double.class);
    protected final Minecraft mc;
    private final ContainerScreen<?> guiContainer;

    public GuiContainerHandler(ContainerScreen<?> guiContainer) {
        this.mc = Minecraft.getInstance();
        this.guiContainer = guiContainer;
    }

    @Override
    public void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed) {
        HANDLE_MOUSE_CLICK.call(this.guiContainer,
                                slot,
                                slot.slotNumber,
                                mouseButton.getValue(),
                                shiftPressed ? ClickType.QUICK_MOVE : ClickType.PICKUP);
    }

    @Override
    public boolean disableRMBDraggingFunctionality() {
        IGNORE_MOUSE_UP.set(this.guiContainer, true);
        if (DRAG_SPLITTING.get(this.guiContainer)) {
            if (DRAG_SPLITTING_BUTTON.get(this.guiContainer) == 1) {
                DRAG_SPLITTING.set(this.guiContainer, false);
                return true;
            }
        }
        return false;
    }

    @Override
    public Slot getSlotUnderMouse(double mouseX, double mouseY) {
        return GET_SELECTED_SLOT.call(this.guiContainer, mouseX, mouseY);
    }

    @Override
    public List<Slot> getSlots() {
        return this.guiContainer.getContainer().inventorySlots;
    }

    @Override
    public boolean isCraftingOutput(Slot slot) {
        return slot instanceof CraftingResultSlot ||
               slot instanceof FurnaceResultSlot ||
               slot instanceof MerchantResultSlot ||
               this.guiContainer.getContainer() instanceof RepairContainer && slot.slotNumber == 2;
    }

    @Override
    public boolean isIgnored(Slot slot) {
        return false;
    }

    @Override
    public boolean isMouseTweaksDisabled() {
        return false;
    }

    @Override
    public boolean isWheelTweakDisabled() {
        return false;
    }
}
