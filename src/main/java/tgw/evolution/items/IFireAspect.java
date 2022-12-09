package tgw.evolution.items;

/**
 * Melee Weapons that implement this interface have a chance of setting the enemy on fire when hitting.
 */
public interface IFireAspect extends IMelee {

    /**
     * @return The level of the Fire Aspect. Each level will burn the target for 4s.
     */
    int fireLevel();
}
