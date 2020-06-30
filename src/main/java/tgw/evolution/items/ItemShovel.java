package tgw.evolution.items;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraftforge.common.ToolType;

import java.util.HashSet;
import java.util.Set;

public class ItemShovel extends ItemTool implements IOffhandAttackable {

    private static final Set<Block> EFFECTIVE_ON = new HashSet<>();
    private static final Set<Material> EFFECTIVE_MATS = Sets.newHashSet(Material.EARTH, Material.ORGANIC, Material.SAND, Material.CLAY, Material.SNOW);

    public ItemShovel(IItemTier tier, float attackSpeedIn, Item.Properties builder) {
        super(attackSpeedIn, tier, EFFECTIVE_ON, EFFECTIVE_MATS, builder.addToolType(ToolType.SHOVEL, tier.getHarvestLevel()));
    }

    @Override
    protected float setReach() {
        return 3.5f;
    }

    @Override
    protected float setBaseDamage() {
        return -1.5F;
    }
}