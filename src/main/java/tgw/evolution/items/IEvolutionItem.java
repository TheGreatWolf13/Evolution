package tgw.evolution.items;

/**
 * Interface present in all {@code Item}s from the Mod to control tooltips.
 */
public interface IEvolutionItem {

    /**
     * @return The slow down rate of this item when it's being used. {@code 1.0} means no slow down, while {@code 0.0} means full totally stopped.
     */
    default double useItemSlowDownRate() {
        return 1.0;
    }
}
