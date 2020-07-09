package tgw.evolution.items;

import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nonnull;

public interface IMelee {

    double getAttackSpeed();

    double getAttackDamage();

    double getReach();

    @Nonnull
    EvolutionDamage.Type getDamageType();
}
