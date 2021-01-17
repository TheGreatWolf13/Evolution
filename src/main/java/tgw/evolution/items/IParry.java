package tgw.evolution.items;

import net.minecraft.item.ItemStack;

/**
 * Items implemented by this interface can parry by holding right-click.
 * Parry will reduce the damage taken.
 */
public interface IParry {

    /**
     * The percentage of the damage that this item will nullify, from {@code 0.0F} to {@code 1.0F}
     */
    float getParryPercentage(ItemStack stack);
}
