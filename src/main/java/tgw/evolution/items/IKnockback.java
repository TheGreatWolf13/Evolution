package tgw.evolution.items;

/**
 * Melee weapons that implement this interface will have increased knockback.
 */
public interface IKnockback extends IMelee {

    /**
     * @return The level of the Knockback.
     */
    int knockbackLevel();
}
