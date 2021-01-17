package tgw.evolution.items;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemTiered extends ItemEv {

    private final IItemTier tier;

    public ItemTiered(IItemTier tier, Item.Properties builder) {
        super(builder.defaultMaxDamage(tier.getMaxUses()));
        this.tier = tier;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return this.tier.getRepairMaterial().test(repair) || super.getIsRepairable(toRepair, repair);
    }

    @Override
    public int getItemEnchantability() {
        return this.tier.getEnchantability();
    }

    public IItemTier getTier() {
        return this.tier;
    }
}
