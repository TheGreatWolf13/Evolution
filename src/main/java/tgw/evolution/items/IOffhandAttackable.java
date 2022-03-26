package tgw.evolution.items;

import net.minecraft.world.item.ItemStack;

public interface IOffhandAttackable {

    double getAttackDamage(ItemStack stack);

    double getAttackSpeed(ItemStack stack);
}
