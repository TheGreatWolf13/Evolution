package tgw.evolution.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.items.modular.ItemModular;

public final class ItemUtils {

    private ItemUtils() {
    }

    public static boolean isAxe(ItemStack stack) {
        return stack.getItem() instanceof ItemModular mod && mod.isAxe(stack);
    }

    public static boolean isCorrectToolForDrops(ItemStack stack, BlockState state, Level level, BlockPos pos) {
        if (stack.getItem() instanceof IEvolutionItem item) {
            return item.isCorrectToolForDrops(stack, state, level, pos);
        }
        return stack.isCorrectToolForDrops(state);
    }

    public static boolean isHammer(ItemStack stack) {
        return stack.getItem() instanceof ItemModular mod && mod.isHammer(stack);
    }

    public static boolean usesModularRendering(ItemStack stack) {
        if (stack.getItem() instanceof IEvolutionItem item) {
            return item.usesModularRendering();
        }
        return false;
    }
}
