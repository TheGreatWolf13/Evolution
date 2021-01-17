package tgw.evolution.items;

public interface ILunge {

    /**
     * @return The time it takes to perform a full lunge, in ticks.
     */
    int getFullLungeTime();

    /**
     * @return The mininum time it takes to perform a mininum power lunge, in ticks.
     */
    int getMinLungeTime();
}
