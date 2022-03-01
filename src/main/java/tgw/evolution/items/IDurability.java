package tgw.evolution.items;

import net.minecraft.world.item.ItemStack;

public interface IDurability {

    default String displayDurability(ItemStack stack) {
        int maxDamage = this.getMaxDmg(stack);
        return (maxDamage - this.getDmg(stack)) + " / " + maxDamage;
    }

    default int getDmg(ItemStack stack) {
        return stack.getItem().getDamage(stack);
    }

    default int getMaxDmg(ItemStack stack) {
        return stack.getItem().getMaxDamage(stack);
    }
}
