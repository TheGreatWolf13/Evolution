package tgw.evolution.items;

import net.minecraft.item.ItemStack;

public interface IDurability {

    default int getMaxDmg(ItemStack stack) {
        return stack.getItem().getMaxDamage();
    }

    default int getDmg(ItemStack stack) {
        return !stack.hasTag() ? 0 : stack.getTag().getInt("Damage");
    }

    default String displayDurability(ItemStack stack) {
        return (this.getMaxDmg(stack) - this.getDmg(stack)) + " / " + this.getMaxDmg(stack);
    }
}
