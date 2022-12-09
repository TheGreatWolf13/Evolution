package tgw.evolution.items;

/**
 * Melee weapons that implement this interface will be able to perform a Heavy Attack.
 * This attack will be able to disable shields and have a chance to stun.
 */
public interface IHeavyAttack extends IMelee {

    /**
     * @return The level of the Heavy Attack.
     * Each level increases the default 4s of shield disabled by 1s.
     * Each level has a 10% of stunning the target.
     */
    int heavyAttackLevel();
}
