package tgw.evolution.items;

/**
 * Interface present in all {@code Item}s from the Mod to control tooltips and other properties.
 */
public interface IEvolutionItem {

    /**
     * @return Whether this item should prevent the player from sprinting and cancel the sprinting if it's being used.
     */
    default boolean useItemPreventsSprinting() {
        return false;
    }

    /**
     * @return The slow down rate of this item when it's being used. {@code 1.0} means no slow down, while {@code 0.0} means full totally stopped.
     */
    default double useItemSlowDownRate() {
        return 1.0;
    }
}
