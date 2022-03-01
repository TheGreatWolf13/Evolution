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

public class ItemAxe extends ItemGenericTool implements ITwoHanded, IHeavyAttack, IBackWeapon {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet();
    private static final Set<Material> EFFECTIVE_MAT = Sets.newHashSet(Material.WOOD, Material.PLANT);
    private final double mass;

    public ItemAxe(ItemMaterial tier, float attackSpeed, Properties builder, double mass) {
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
    public EvolutionDamage.Type getDamageType(ItemStack stack) {
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
    public double getMass(ItemStack stack) {
        return this.mass;
    }

    @Override
    public IModular getModularCap(ItemStack stack) {
        return IModular.NULL;
    }

    @Override
    public int getPriority(ItemStack stack) {
        return 2;
    }

    @Override
    public float reach() {
        return 3;
    }
}
