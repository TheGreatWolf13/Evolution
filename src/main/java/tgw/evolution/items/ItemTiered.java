package tgw.evolution.items;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemTiered extends ItemEv {

    private final IItemTier tier;

    public ItemTiered(IItemTier tierIn, Item.Properties builder) {
        super(builder.defaultMaxDamage(tierIn.getMaxUses()));
        this.tier = tierIn;
    }

    public IItemTier getTier() {
        return this.tier;
    }

    @Override
    public int getItemEnchantability() {
        return this.tier.getEnchantability();
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return this.tier.getRepairMaterial().test(repair) || super.getIsRepairable(toRepair, repair);
    }
}
