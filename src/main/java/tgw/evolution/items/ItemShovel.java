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

public class ItemShovel extends ItemGenericTool implements IBackWeapon {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet();
    private static final Set<Material> EFFECTIVE_MATS = Sets.newHashSet(Material.DIRT, Material.GRASS, Material.SAND, Material.CLAY, Material.SNOW);
    private final double mass;

    public ItemShovel(ItemMaterial tier, float attackSpeed, Properties builder, double mass) {
        super(attackSpeed, tier, EFFECTIVE_ON, EFFECTIVE_MATS, builder, ToolType.SHOVEL);
        this.mass = mass;
    }

    @Override
    public float baseDamage() {
        return -3.0f;
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
    public double getMass(ItemStack stack) {
        return this.mass;
    }

    @Override
    public IModular getModularCap(ItemStack stack) {
        return IModular.NULL;
    }

    @Override
    public int getPriority(ItemStack stack) {
        return 4;
    }

    @Override
    public float reach() {
        return 3.5f;
    }
}