package tgw.evolution.items;

import com.google.common.collect.Sets;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.IModular;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.ItemMaterial;

import javax.annotation.Nonnull;
import java.util.Set;

public class ItemHammer extends ItemGenericTool implements IHeavyAttack {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet();
    private static final Set<Material> EFFECTIVE_MAT = Sets.newHashSet();

    private final double mass;

    public ItemHammer(ItemMaterial tier, float attackSpeed, Properties builder, double mass) {
        super(attackSpeed, tier, EFFECTIVE_ON, EFFECTIVE_MAT, builder, ToolType.HAMMER);
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
    public EvolutionDamage.Type getDamageType(ItemStack stack) {
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
    public double getMass(ItemStack stack) {
        return this.mass;
    }

    @Override
    public IModular getModularCap(ItemStack stack) {
        return IModular.NULL;
    }

    @Override
    public float reach() {
        return 3;
    }
}
