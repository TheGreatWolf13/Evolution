package tgw.evolution.items;

import net.minecraft.item.BucketItem;
import tgw.evolution.init.EvolutionFluids;
import tgw.evolution.init.EvolutionItems;

public class ItemFreshWaterBucket extends BucketItem {

    public ItemFreshWaterBucket() {
        super(EvolutionFluids.FRESH_WATER, EvolutionItems.propMisc().maxStackSize(1));
    }
}
