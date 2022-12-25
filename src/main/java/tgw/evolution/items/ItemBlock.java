package tgw.evolution.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ItemBlock extends BlockItem implements IEvolutionItem {

    public ItemBlock(Block block, Properties builder) {
        super(block, builder);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state, @Nullable Level level, @Nullable BlockPos pos) {
        return false;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.matches(oldStack, newStack);
    }
}
