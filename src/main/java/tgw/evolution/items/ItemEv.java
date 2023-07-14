package tgw.evolution.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ItemEv extends Item implements IEvolutionItem {

    public ItemEv(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state, @Nullable Level level, @Nullable BlockPos pos) {
        return false;
    }
}
