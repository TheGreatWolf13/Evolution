package tgw.evolution.entities;

public interface IMammal {

    /**
     * @return The time is ticks the female will produce milk after giving birth.
     */
    int getLactationPeriod();

    /**
     * @return Gets the current lactation time of the entity, in ticks.
     */
    int getLactationTime();

    /**
     * @param lactationTime Sets the current lactation time of the entity, in ticks.
     */
    void setLactationTime(int lactationTime);
}
