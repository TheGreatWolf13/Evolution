package tgw.evolution.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.items.modular.ItemModularTool;

public final class ItemUtils {

    private ItemUtils() {
    }

    public static boolean canRepeatUse(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ItemBlock || item instanceof BlockItem;
    }

    public static double getDmgMultiplier(ItemStack stack, EvolutionDamage.Type type) {
        return stack.getItem() instanceof ItemModularTool mod ? mod.getDmgMultiplier(stack, type) : 1;
    }

    public static double getMass(ItemStack stack) {
        return stack.getItem() instanceof ItemModular mod ? mod.getMass(stack) : 0;
    }

    public static boolean isAxe(ItemStack stack) {
        return stack.getItem() instanceof ItemModular mod && mod.isAxe(stack);
    }

    public static boolean isCorrectToolForDrops(ItemStack stack, BlockState state, Level level, int x, int y, int z) {
        if (stack.getItem() instanceof IEvolutionItem item) {
            return item.isCorrectToolForDrops(stack, state, level, x, y, z);
        }
        return stack.isCorrectToolForDrops(state);
    }

    public static boolean isHammer(ItemStack stack) {
        return stack.getItem() instanceof ItemModular mod && mod.isHammer(stack);
    }

    public static boolean isSameIgnoreCount(ItemStack stack, ItemStack other) {
        if (stack.isEmpty() && other.isEmpty()) {
            return true;
        }
        return !stack.isEmpty() && !other.isEmpty() && matches(stack, other);
    }

    private static boolean matches(ItemStack stack, ItemStack other) {
        if (!stack.is(other.getItem())) {
            return false;
        }
        if (stack.getTag() == null && other.getTag() != null) {
            return false;
        }
        return stack.getTag() == null || stack.getTag().equals(other.getTag());
    }

    public static boolean usesModularRendering(ItemStack stack) {
        if (stack.getItem() instanceof IEvolutionItem item) {
            return item.usesModularRendering();
        }
        return false;
    }
}
