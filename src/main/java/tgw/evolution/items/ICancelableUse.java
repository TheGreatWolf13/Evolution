package tgw.evolution.items;

import net.minecraft.world.item.ItemStack;

public interface ICancelableUse {

    default boolean isCancelable(ItemStack stack) {
        return true;
    }
}
