package tgw.evolution.items;

import net.minecraft.world.item.ItemStack;

/**
 * Used to write {@code Two Handed} in the item tooltip.
 */
public interface ITwoHanded {

    default boolean isTwoHanded(ItemStack stack) {
        return true;
    }
}
