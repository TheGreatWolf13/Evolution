package tgw.evolution.items;

/**
 * Melee weapons that implement this interface will be able to perform a Heavy Attack by chance.
 * This attack will be able to disable shields and deal more damage.
 */
public interface IHeavyAttack {

    /**
     * @return The chance that the attack will be a Heavy Attack, given in a float from 0 to 1f. Sprinting doubles this chance.
     */
    float getChance();

    /**
     * @return The level of the Heavy Attack.
     * Sprinting doubles this value.
     * Each level increases the default 4s of shield disabled by 1s.
     * Each level increases damage by 10%.
     */
    int getLevel();
}
