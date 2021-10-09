package tgw.evolution.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.util.reflection.BiFunctionMethodHandler;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.util.reflection.TetraFunctionMethodHandler;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GuiContainerHandler implements IGuiScreenHandler {
    private static final TetraFunctionMethodHandler<ContainerScreen, Void, Slot, Integer, Integer, ClickType> HANDLE_MOUSE_CLICK =
            new TetraFunctionMethodHandler<>(
            ContainerScreen.class,
            "func_184098_a",
            Slot.class,
            int.class,
            int.class,
            ClickType.class);
    private static final FieldHandler<ContainerScreen, Boolean> IGNORE_MOUSE_UP = new FieldHandler<>(ContainerScreen.class, "field_146995_H");
    private static final FieldHandler<ContainerScreen, Boolean> DRAG_SPLITTING = new FieldHandler<>(ContainerScreen.class, "field_147007_t");
    private static final FieldHandler<ContainerScreen, Integer> DRAG_SPLITTING_BUTTON = new FieldHandler<>(ContainerScreen.class, "field_146988_G");
    private static final BiFunctionMethodHandler<ContainerScreen, Slot, Double, Double> GET_SELECTED_SLOT = new BiFunctionMethodHandler<>(
            ContainerScreen.class,
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
        HANDLE_MOUSE_CLICK.call(this.guiContainer, slot, slot.index, mouseButton.getValue(), shiftPressed ? ClickType.QUICK_MOVE : ClickType.PICKUP);
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
        return this.guiContainer.getMenu().slots;
    }

    @Override
    public boolean isCraftingOutput(Slot slot) {
        return slot instanceof CraftingResultSlot ||
               slot instanceof FurnaceResultSlot ||
               slot instanceof MerchantResultSlot ||
               this.guiContainer.getMenu() instanceof RepairContainer && slot.index == 2;
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
