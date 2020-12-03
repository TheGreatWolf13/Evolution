package tgw.evolution.items;

/**
 * Melee Weapons that implement this interface have a chance of setting the enemy on fire when hitting.
 */
public interface IFireAspect {

    /**
     * @return The chance of the Fire Aspect to activate, given in a float from 0 to 1f.
     */
    float getChance();

    /**
     * @return The level of the Fire Aspect. Each level will burn the target for 4s.
     */
    int getLevel();
}
