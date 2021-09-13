package tgw.evolution.items;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.IItemTier;
import net.minecraftforge.common.ToolType;
import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nonnull;
import java.util.Set;

public class ItemAxe extends ItemGenericTool implements ITwoHanded, IHeavyAttack, IBackWeapon {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet();
    private static final Set<Material> EFFECTIVE_MAT = Sets.newHashSet(Material.WOOD, Material.PLANT);
    private final double mass;

    public ItemAxe(IItemTier tier, float attackSpeed, Properties builder, double mass) {
        super(attackSpeed, tier, EFFECTIVE_ON, EFFECTIVE_MAT, builder, ToolType.AXE);
        this.mass = mass;
    }

    @Override
    public float baseDamage() {
        return 8.0f;
    }

    @Override
    public int blockDurabilityDamage() {
        return 1;
    }

    @Override
    public int entityDurabilityDamage() {
        return 1;
    }

    @Nonnull
    @Override
    public EvolutionDamage.Type getDamageType() {
        return EvolutionDamage.Type.SLASHING;
    }

    @Override
    public float getHeavyAttackChance() {
        return 0.25f;
    }

    @Override
    public int getHeavyAttackLevel() {
        return 2;
    }

    @Override
    public double getMass() {
        return this.mass;
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public float reach() {
        return 3;
    }
}
