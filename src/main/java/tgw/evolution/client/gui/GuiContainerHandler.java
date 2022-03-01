package tgw.evolution.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.util.reflection.BiFunctionMethodHandler;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.util.reflection.TetraFunctionMethodHandler;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GuiContainerHandler implements IGuiScreenHandler {
    private static final TetraFunctionMethodHandler<AbstractContainerScreen, Void, Slot, Integer, Integer, ClickType> SLOT_CLICKED =
            new TetraFunctionMethodHandler<>(
            AbstractContainerScreen.class,
            "m_6597_",
            Slot.class,
            int.class,
            int.class,
            ClickType.class);
    private static final FieldHandler<AbstractContainerScreen, Boolean> SKIP_NEXT_RELEASE = new FieldHandler<>(AbstractContainerScreen.class,
                                                                                                               "f_97719_");
    private static final FieldHandler<AbstractContainerScreen, Boolean> IS_QUICK_CRAFTING = new FieldHandler<>(AbstractContainerScreen.class,
                                                                                                               "f_97738_");
    private static final FieldHandler<AbstractContainerScreen, Integer> QUICK_CRAFTING_BUTTON = new FieldHandler<>(AbstractContainerScreen.class,
                                                                                                                   "f_97718_");
    private static final BiFunctionMethodHandler<AbstractContainerScreen, Slot, Double, Double> FIND_SLOT = new BiFunctionMethodHandler<>(
            AbstractContainerScreen.class,
            "m_182417_",
            double.class,
            double.class);
    protected final Minecraft mc;
    private final AbstractContainerScreen<?> containerScreen;

    public GuiContainerHandler(AbstractContainerScreen<?> containerScreen) {
        this.mc = Minecraft.getInstance();
        this.containerScreen = containerScreen;
    }

    @Override
    public void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed) {
        SLOT_CLICKED.call(this.containerScreen, slot, slot.index, mouseButton.getValue(), shiftPressed ? ClickType.QUICK_MOVE : ClickType.PICKUP);
    }

    @Override
    public boolean disableRMBDraggingFunctionality() {
        SKIP_NEXT_RELEASE.set(this.containerScreen, true);
        if (IS_QUICK_CRAFTING.get(this.containerScreen) && QUICK_CRAFTING_BUTTON.get(this.containerScreen) == 1) {
            IS_QUICK_CRAFTING.set(this.containerScreen, false);
            return true;
        }
        return false;
    }

    @Override
    public Slot getSlotUnderMouse(double mouseX, double mouseY) {
        return FIND_SLOT.call(this.containerScreen, mouseX, mouseY);
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
