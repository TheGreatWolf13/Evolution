package tgw.evolution.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemEv extends Item implements IEvolutionItem {

    public ItemEv(Properties properties) {
        super(properties);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.areItemStacksEqual(oldStack, newStack);
    }
}
