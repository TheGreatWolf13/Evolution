package tgw.evolution.items;

import net.minecraft.world.item.ItemStack;

/**
 * Implemented Items will be rendered in the player's back
 */
public interface IBackWeapon {

    /**
     * @return The priority of the item when rendering, 0 being the most priority, bigger numbers being lower priority. Negative numbers simply
     * represent no priority at all, not even rendering.
     */
    int getPriority(ItemStack stack);
}
