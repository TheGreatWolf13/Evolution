package tgw.evolution.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ItemBlock extends BlockItem implements IEvolutionItem {

    public ItemBlock(Block block, Properties builder) {
        super(block, builder);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.matches(oldStack, newStack);
    }
}
