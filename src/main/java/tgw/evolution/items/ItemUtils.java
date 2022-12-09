package tgw.evolution.items;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.items.modular.ItemModular;

public final class ItemUtils {

    private ItemUtils() {
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
