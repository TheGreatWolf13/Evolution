package tgw.evolution.items;

public interface IFireAspect {

    /**
     * @return The time in seconds * 4 that the entity will be on fire.
     */
    int getModifier();

    /**
     * @return The chance of the Fire Aspect to activate, given in a float from 0 to 1f.
     */
    float getChance();
}
