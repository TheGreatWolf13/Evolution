package tgw.evolution.items;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.IItemTier;

import java.util.Set;

public class ItemHammer extends ItemTool {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet();
    private static final Set<Material> EFFECTIVE_MAT = Sets.newHashSet();

    public ItemHammer(IItemTier tier, float attackSpeedIn, Properties builder) {
        super(attackSpeedIn, tier, EFFECTIVE_ON, EFFECTIVE_MAT, builder.addToolType(ToolTypeEv.HAMMER, tier.getHarvestLevel()));
    }

    @Override
    protected float setReach() {
        return 3;
    }

    @Override
    protected float setBaseDamage() {
        return 1.5f;
    }
}
