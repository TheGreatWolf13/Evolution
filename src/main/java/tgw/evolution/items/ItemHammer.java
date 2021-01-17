package tgw.evolution.items;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.IItemTier;
import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nonnull;
import java.util.Set;

public class ItemHammer extends ItemGenericTool implements IHeavyAttack {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet();
    private static final Set<Material> EFFECTIVE_MAT = Sets.newHashSet();
    private final double mass;

    public ItemHammer(IItemTier tier, float attackSpeed, Properties builder, double mass) {
        super(attackSpeed, tier, EFFECTIVE_ON, EFFECTIVE_MAT, builder, ToolTypeEv.HAMMER);
        this.mass = mass;
    }

    @Override
    public float baseDamage() {
        return 6.0f;
    }

    @Override
    public int blockDurabilityDamage() {
        return 1;
    }

    @Override
    public int entityDurabilityDamage() {
        return 2;
    }

    @Nonnull
    @Override
    public EvolutionDamage.Type getDamageType() {
        return EvolutionDamage.Type.CRUSHING;
    }

    @Override
    public float getHeavyAttackChance() {
        return 0.2f;
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
    public float reach() {
        return 3;
    }
}
