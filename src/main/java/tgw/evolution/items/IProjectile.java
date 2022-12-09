package tgw.evolution.items;

import tgw.evolution.init.EvolutionDamage;

public interface IProjectile {

    boolean isDamageProportionalToMomentum();

    /**
     * @return A value from {@code 0.0f} to {@code 1.0f}, representing the precision of this {@link IProjectile}.
     */
    float precision();

    EvolutionDamage.Type projectileDamageType();

    /**
     * @return The speed at which this {@link IProjectile} will be launched, in meters/tick.
     */
    double projectileSpeed();
}
