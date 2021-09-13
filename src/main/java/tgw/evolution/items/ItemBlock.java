package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class ItemBlock extends BlockItem implements IEvolutionItem {

    public ItemBlock(Block block, Properties builder) {
        super(block, builder);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.matches(oldStack, newStack);
    }
}
