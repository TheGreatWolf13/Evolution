package tgw.evolution.items;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.IItemTier;
import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nonnull;
import java.util.Set;

public class ItemHammer extends ItemTool {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet();
    private static final Set<Material> EFFECTIVE_MAT = Sets.newHashSet();
    private final double mass;

    public ItemHammer(IItemTier tier, float attackSpeed, Properties builder, double mass) {
        super(attackSpeed, tier, EFFECTIVE_ON, EFFECTIVE_MAT, builder.addToolType(ToolTypeEv.HAMMER, tier.getHarvestLevel()));
        this.mass = mass;
    }

    @Override
    protected float setBaseDamage() {
        return 6.0f;
    }

    @Override
    protected float setReach() {
        return 3;
    }

    @Override
    public double getMass() {
        return this.mass;
    }

    @Nonnull
    @Override
    public EvolutionDamage.Type getDamageType() {
        return EvolutionDamage.Type.CRUSHING;
    }
}
