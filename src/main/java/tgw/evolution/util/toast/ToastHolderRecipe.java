package tgw.evolution.util.toast;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ToastHolderRecipe extends ToastHolder {

    private final ItemStack showItem;
    private final ItemStack toastSymbol;

    public ToastHolderRecipe(ItemStack showItem, ItemStack toastSymbol) {
        this.showItem = showItem;
        this.toastSymbol = toastSymbol;
    }

    public ToastHolderRecipe(Item showItem, Item toastSymbol) {
        this(new ItemStack(showItem), new ItemStack(toastSymbol));
    }

    public ToastHolderRecipe(ItemStack showItem, Item toastSymbol) {
        this(showItem, new ItemStack(toastSymbol));
    }

    @Override
    public ItemStack getShowItem() {
        return this.showItem;
    }

    @Override
    public ItemStack getToastSymbol() {
        return this.toastSymbol;
    }
}
