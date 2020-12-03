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

public class ItemAxe extends ItemGenericTool implements ITwoHanded, IHeavyAttack {

    private static final Set<Block> EFFECTIVE_ON = new HashSet<>();
    private static final Set<Material> EFFECTIVE_MAT = Sets.newHashSet(Material.WOOD, Material.PLANTS);
    private final double mass;

    public ItemAxe(IItemTier tier, float attackSpeed, Properties builder, double mass) {
        super(attackSpeed, tier, EFFECTIVE_ON, EFFECTIVE_MAT, builder.addToolType(ToolType.AXE, tier.getHarvestLevel()));
        this.mass = mass;
    }

    @Override
    public float getChance() {
        return 0.25f;
    }

    @Nonnull
    @Override
    public EvolutionDamage.Type getDamageType() {
        return EvolutionDamage.Type.SLASHING;
    }

    @Override
    public int getLevel() {
        return 2;
    }

    @Override
    public double getMass() {
        return this.mass;
    }

    @Override
    public float setBaseDamage() {
        return 8.0f;
    }

    @Override
    public float setReach() {
        return 3;
    }
}
