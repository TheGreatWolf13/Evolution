package tgw.evolution.items;

import java.util.Random;

public interface IFireAspect {

    /**
     * @return The time in seconds * 4 that the entity will be on fire.
     */
    int getModifier();

    boolean activate(Random rand);
}
