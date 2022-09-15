package tgw.evolution.client.gui;

import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.util.MouseButton;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public interface IGuiScreenHandler {

    void clickSlot(Slot slot, @MouseButton int mouseButton, boolean shiftPressed);

    @Nullable Slot getSlotUnderMouse(double mouseX, double mouseY);

    List<Slot> getSlots();

    boolean isCraftingOutput(Slot slot);

    boolean isIgnored(Slot slot);
}
