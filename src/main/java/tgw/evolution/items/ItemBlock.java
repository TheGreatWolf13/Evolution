package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class ItemBlock extends BlockItem {

    public ItemBlock(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.areItemStacksEqual(oldStack, newStack);
    }
}
