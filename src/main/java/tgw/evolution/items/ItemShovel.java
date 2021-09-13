package tgw.evolution.items;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.IItemTier;
import net.minecraftforge.common.ToolType;
import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nonnull;
import java.util.Set;

public class ItemShovel extends ItemGenericTool implements IBackWeapon {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet();
    private static final Set<Material> EFFECTIVE_MATS = Sets.newHashSet(Material.DIRT, Material.GRASS, Material.SAND, Material.CLAY, Material.SNOW);
    private final double mass;

    public ItemShovel(IItemTier tier, float attackSpeed, Properties builder, double mass) {
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
    public EvolutionDamage.Type getDamageType() {
        return EvolutionDamage.Type.CRUSHING;
    }

    @Override
    public double getMass() {
        return this.mass;
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public float reach() {
        return 3.5f;
    }
}