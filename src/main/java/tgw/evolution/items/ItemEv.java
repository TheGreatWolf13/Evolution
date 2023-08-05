package tgw.evolution.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemEv extends Item implements IEvolutionItem {

    public ItemEv(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state, Level level, int x, int y, int z) {
        return false;
    }
}
