package tgw.evolution.items;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemTiered extends ItemEv {

    private final IItemTier tier;

    public ItemTiered(IItemTier tier, Item.Properties builder) {
        super(builder.defaultDurability(tier.getUses()));
        this.tier = tier;
    }

    @Override
    public int getEnchantmentValue() {
        return this.tier.getEnchantmentValue();
    }

    public IItemTier getTier() {
        return this.tier;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return this.tier.getRepairIngredient().test(repair) || super.isValidRepairItem(toRepair, repair);
    }
}
