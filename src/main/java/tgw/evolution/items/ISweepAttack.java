package tgw.evolution.items;

/**
 * Melee weapons that implement this interface can attack multiple targets at the same time.
 */
public interface ISweepAttack {

    /**
     * @return The percentage of damage that the sweep attack will deal. 0.0f means nothing, 1.0f means full strength.
     */
    float getSweepRatio();
}
