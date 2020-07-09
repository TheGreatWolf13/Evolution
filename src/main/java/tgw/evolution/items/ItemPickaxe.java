package tgw.evolution.items;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.IItemTier;
import net.minecraftforge.common.ToolType;
import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class ItemPickaxe extends ItemTool {

    private static final Set<Block> EFFECTIVE_ON = new HashSet<>();
    private static final Set<Material> EFFECTIVE_MAT = Sets.newHashSet(Material.ROCK, Material.IRON);
    private final double mass;

    public ItemPickaxe(IItemTier tier, float attackSpeed, Properties builder, double mass) {
        super(attackSpeed, tier, EFFECTIVE_ON, EFFECTIVE_MAT, builder.addToolType(ToolType.PICKAXE, tier.getHarvestLevel()));
        this.mass = mass;
    }

    @Override
    protected float setReach() {
        return 3;
    }

    @Override
    protected float setBaseDamage() {
        return -2;
    }

    @Override
    public double getMass() {
        return this.mass;
    }

    @Nonnull
    @Override
    public EvolutionDamage.Type getDamageType() {
        return EvolutionDamage.Type.PIERCING;
    }
}
