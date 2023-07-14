package tgw.evolution.inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class SlotTexturedHandler extends SlotTextured {

    public SlotTexturedHandler(@SuppressWarnings("TypeMayBeWeakened") BasicContainer container,
                               int slotIndex,
                               int x,
                               int y,
                               @Nullable ResourceLocation icon) {
        super(container, slotIndex, x, y, icon);
    }

    @Override
    public void onTake(Player player, ItemStack takenStack) {
        super.onTake(player, takenStack);
        ((BasicContainer) this.container).onTake(this.getContainerSlot(), player, takenStack, this.getItem().copy());
    }
}
