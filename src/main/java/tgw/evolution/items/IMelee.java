package tgw.evolution.items;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nonnull;

public interface IMelee {

    double getAttackDamage(ItemStack stack);

    double getAttackSpeed(ItemStack stack);

    @Nonnull
    EvolutionDamage.Type getDamageType(ItemStack stack);
}
